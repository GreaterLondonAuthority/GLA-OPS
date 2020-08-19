package uk.gov.london.ops.project.migration

import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.ReflectionUtils
import org.springframework.util.StringUtils
import uk.gov.london.ops.audit.AuditService
import uk.gov.london.ops.framework.exception.ValidationException
import uk.gov.london.ops.project.ProjectService
import uk.gov.london.ops.project.block.NamedProjectBlock
import uk.gov.london.ops.project.deliverypartner.DeliveryPartnersBlock
import uk.gov.london.ops.project.question.Answer
import uk.gov.london.ops.project.question.ProjectQuestionsBlock
import uk.gov.london.ops.project.repeatingentity.EntityCollection
import uk.gov.london.ops.project.repeatingentity.OtherFundingBlock
import uk.gov.london.ops.project.template.TemplateProjectService
import uk.gov.london.ops.project.template.TemplateService
import uk.gov.london.ops.project.template.domain.AnswerType
import uk.gov.london.ops.project.template.domain.QuestionsTemplateBlock
import uk.gov.london.ops.project.template.domain.TemplateBlock
import uk.gov.london.ops.user.UserService
import java.lang.reflect.Method
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.persistence.EntityManager
import javax.transaction.Transactional
import kotlin.reflect.jvm.jvmName

@Transactional
@Service
class BlockMigrationService @Autowired constructor(
        val projectService: ProjectService,
        val templateService: TemplateService,
        val templateProjectService: TemplateProjectService,
        val userService: UserService,
        val auditService: AuditService,
        val entityManager: EntityManager) {

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")


    /**
     * Migrate the specified blocks
     */
    fun migrateBlockData(migrationRequest: MigrationRequest): MigrationResponse {
        val template = templateService.find(migrationRequest.templateId)

        val source = template.getBlockById(migrationRequest.sourceBlockId)
        val destination = template.getBlockById(migrationRequest.destinationBlockId)

        validateRequest(source, destination, migrationRequest)

        val allByTemplate = templateProjectService.findAllByTemplate(template)
        var migrationResponse = MigrationResponse()

        for (project in allByTemplate) {
            var sourceBlock = project.getBlockByTypeDisplayOrderAndLatestVersion(source.block, source.displayOrder)
            var targetBlock = project.getBlockByTypeDisplayOrderAndLatestVersion(destination.block, destination.displayOrder)

            migrateBlock(sourceBlock, targetBlock, migrationRequest, migrationResponse)
            entityManager.detach(project)
        }
        auditService.auditActivityForUser(userService.currentUsername(), "Data from block ${migrationRequest.sourceBlockId} migrated to block ${migrationRequest.destinationBlockId} for ${migrationResponse.migratedProjects.size} projects using template ${migrationRequest.templateId}")

        return migrationResponse
    }

    fun validateRequest(source: TemplateBlock, destination: TemplateBlock, migrationRequest: MigrationRequest) {
        if (destination.block != migrationRequest.projectBlockType) {
            throw ValidationException("Destination block doesn't match requested block type")
        }

        if (source !is QuestionsTemplateBlock) {
            throw  ValidationException("Source block is not a questions block")
        }
    }

    /**
     * This method modifies/stores block data in a separate transaction and
     * assumes targetBlock changes are not saved again in the calling method / transaction
     */
    fun migrateBlock(sourceBlock: NamedProjectBlock, targetBlock: NamedProjectBlock, migrationRequest: MigrationRequest, migrationResponse: MigrationResponse) {
        val sourceQuestions: ProjectQuestionsBlock = sourceBlock as ProjectQuestionsBlock

        if (!validProjectForMigration(targetBlock, migrationRequest, migrationResponse)) {
            return
        }

        val destination = targetBlock as EntityCollection<Any>

        val success = ProjectMigrationSuccess(sourceBlock.projectId)

        val blockMigrationsMap = populateEntityFromQuestions(targetBlock, migrationRequest.mappedBlockFields, sourceQuestions)
        if (blockMigrationsMap.keys.isNotEmpty()) {
            success.updatedBlock = blockMigrationsMap
        }

        for (listItem in migrationRequest.mappedFields) {
            val entity = destination.newEntityInstance
            val resultMap = populateEntityFromQuestions(entity, listItem, sourceQuestions)
            var allAnswersNull = resultMap.keys.isEmpty()
            if (!allAnswersNull) {
                success.updatedEntities.add(resultMap)
                if (!migrationRequest.dryRun) {
                    targetBlock.createChildEntity(entity)
                }
            }
        }
        if (!migrationRequest.dryRun) {
            try {
                beforeMigrationSave(targetBlock)
                projectService.updateNamedProjectBlockNewTx(targetBlock, targetBlock.projectId)
            } catch (e: Exception) {
                val rootCause = ExceptionUtils.getRootCause(e)
                migrationResponse.migrationFailures.add(ProjectMigrationFailure(sourceBlock.projectId, rootCause.message!!))
                return
            }
        }
        migrationResponse.migratedProjects.add(success)

    }

    private fun beforeMigrationSave(targetBlock: EntityCollection<Any>) {
        if(targetBlock is OtherFundingBlock){
            (targetBlock as OtherFundingBlock).hasFundingPartners = targetBlock.hasChildEntities()
        }

        if(targetBlock is DeliveryPartnersBlock){
            (targetBlock as DeliveryPartnersBlock).hasDeliveryPartners = targetBlock.hasChildEntities()
        }
    }

    fun populateEntityFromQuestions(entity: Any, listItem: Map<String, Any>, sourceQuestions: ProjectQuestionsBlock): MutableMap<String, Any> {
        val resultMap = mutableMapOf<String, Any>()

        for (entry in listItem) {
            val answer = sourceQuestions.getAnswerByQuestionId(entry.value as Int)
            if (answer != null && answer.answerAsText != null) {
                populateFieldFromQuestion(entity, entry.key, answer)
                resultMap[entry.key] = answer.answerAsText
            }
        }
        return resultMap
    }

    fun validProjectForMigration(targetBlock: NamedProjectBlock, migrationRequest: MigrationRequest, migrationResponse: MigrationResponse): Boolean {
        if (targetBlock is EntityCollection<*>) {
            if (targetBlock.hasChildEntities() || hasStringFieldsModified(targetBlock, migrationRequest.mappedBlockFields.keys)) {
                migrationResponse.migrationFailures.add(ProjectMigrationFailure(targetBlock.projectId, "Project already has data populated in ${targetBlock.blockType}"))
                return false
            }
        } else {
            throw ValidationException("Destination block is not an EntityCollection block")
        }
        return true
    }

    fun hasStringFieldsModified(targetBlock: NamedProjectBlock, fieldNames: MutableSet<String>): Boolean {
        for (fieldName in fieldNames) {
            val field = ReflectionUtils.findField(targetBlock.javaClass, fieldName)
            if (field != null) {
                field.isAccessible = true
                val fieldValue = field.get(targetBlock)
                if (fieldValue is String && !StringUtils.isEmpty(fieldValue)) {
                    return true
                }
            }
        }
        return false
    }

    private fun populateFieldFromQuestion(entity: Any, fieldName: String, answer: Answer) {
        when (answer.question.answerType) {
            AnswerType.Text, AnswerType.FreeText, AnswerType.Dropdown -> answerTextBasedQuestion(entity, fieldName, answer)
            AnswerType.Number -> answerNumericBasedQuestion(entity, fieldName, answer)
            AnswerType.YesNo -> answerYesNoQuestion(entity, fieldName, answer)
            AnswerType.Date -> answerDateBasedQuestion(entity, fieldName, answer)
            else -> throw ValidationException("Unsupported answer type: " + answer.question.answerType)
        }

    }

    private fun answerYesNoQuestion(entity: Any, fieldName: String, answer: Answer) {
        val method = getSetterMethod(entity, fieldName)
        method.invoke(entity, answer.answer.toLowerCase().equals("yes"))
    }

    private fun answerTextBasedQuestion(entity: Any, fieldName: String, answer: Answer) {
        val method = getSetterMethod(entity, fieldName)
        method.invoke(entity, answer.answer)
    }

    private fun answerDateBasedQuestion(entity: Any, fieldName: String, answer: Answer) {
        val method = getSetterMethod(entity, fieldName)
        method.invoke(entity, LocalDate.parse(answer.answer, dateFormat))


    }

    private fun answerNumericBasedQuestion(entity: Any, fieldName: String, answer: Answer) {
        val method = getSetterMethod(entity, fieldName)
        when (method.parameters[0].type) {
            BigDecimal::class.java -> method.invoke(entity, BigDecimal(answer.numericAnswer))
            Double::class.java -> method.invoke(entity, answer.numericAnswer)
            Integer::class.java -> method.invoke(entity, answer.numericAnswer.toInt())
            else -> throw ValidationException("${method.parameters[0].type} type is not supported for answer to $fieldName")
        }


    }

    private fun getSetterMethod(entity: Any, fieldName: String): Method {
        try {
            val field = entity.javaClass.getDeclaredField(fieldName)
            return entity.javaClass.getMethod(getMethodNameFromField(fieldName), field.type)
        } catch (e: Exception) {
            throw ValidationException("Unable to find field $fieldName on ${entity::class.jvmName}: ${e.message}")
        }
    }

    private fun getMethodNameFromField(key: String): String {
        var propName = key
        if (key.startsWith("is")) {
            propName = key.substring(2)
        }
        return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1)
    }


}
