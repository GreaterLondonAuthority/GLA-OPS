/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ops.framework.exception.ValidationException
import uk.gov.london.ops.permission.PermissionServiceImpl
import uk.gov.london.ops.project.ProjectBlockSummary
import uk.gov.london.ops.project.implementation.repository.InternalProjectBlockRepository
import uk.gov.london.ops.project.implementation.repository.ProjectBlockOverviewRepository
import uk.gov.london.ops.project.implementation.repository.ProjectBlockRepository
import uk.gov.london.ops.project.implementation.repository.ProjectOverviewRepository
import uk.gov.london.ops.project.internalblock.InternalBlockType
import uk.gov.london.ops.user.UserUtils.currentUser
import javax.persistence.EntityManager

@Service
class ProjectBlockService @Autowired constructor(val projectBlockRepository: ProjectBlockRepository,
                                                 val projectOverviewRepository: ProjectOverviewRepository,
                                                 val projectBlockOverviewRepository: ProjectBlockOverviewRepository,
                                                 val internalProjectBlockRepository: InternalProjectBlockRepository,
                                                 val projectBlockActivityMap: ProjectBlockActivityMap,
                                                 val entityManager: EntityManager,
                                                 val permissionService: PermissionServiceImpl) {

    fun getProjectBlock(blockId: Int): NamedProjectBlock {
        val blockType = projectBlockRepository.getBlockType(blockId)
        val projectBlockClass = ProjectBlockType.valueOf(blockType).projectBlockClass

        val  block = entityManager.createQuery("select pb from project_block pb where pb.id = " + blockId +
                " and type(pb) = " + projectBlockClass.canonicalName).singleResult
                ?: throw ValidationException("Unable to find block with id: $blockId");

        return block as NamedProjectBlock;
    }

    fun enrichedBlock(block: NamedProjectBlock): NamedProjectBlock {
        val currentUser = currentUser()

        val projectOverview = projectOverviewRepository.findByIdForUser(block.projectId, currentUser.username)
        block.projectOverview = projectOverview

        val pacl = permissionService.getProjectAccessControlList(block.projectId)

        block.allowedActions = projectBlockActivityMap.getAllowedActionsFor(projectOverview, block, currentUser, pacl)

        if (block.blockType.dependsOn != null) {
            enrichFromDependantBlock(block, projectOverview.id)
        }

        return block
    }

    fun enrichFromDependantBlock(block: NamedProjectBlock, projectId: Int?) {
        val otherBlockOverview = projectBlockOverviewRepository.findByProjectIdAndBlockType(projectId, block.blockType.dependsOn)

        val otherBlock: NamedProjectBlock = getProjectBlock(otherBlockOverview.projectBlockId)

        if (otherBlock.blockType.dependsOn != null) {
            throw RuntimeException("Unable to base a block on another dependant block")
        }

        block.enrichFromBlock(otherBlock)
    }

    fun getLearningGrantBlockByProjectIdVersionNumber(projectId: Int, versionNumber: Int, displayOrder: Int): NamedProjectBlock? {
         return projectBlockRepository.findLearningGrantBlockByProjectIdAndVersionNumberAndDisplayOrder(projectId, versionNumber, displayOrder)
    }

    fun findAllLearningGrantBlocksForProject(projectId: Int): Set<NamedProjectBlock> {
         return projectBlockRepository.findAllLearningGrantBlocksForProject(projectId)
    }

    fun forceBlockUpdate(block: NamedProjectBlock) {
        projectBlockRepository.save(block)
    }

    fun getInternalAssessmentBlockSummary(projectId: Int): ProjectBlockSummary {
        val block = internalProjectBlockRepository.findByProjectIdAndType(projectId, InternalBlockType.Assessment)
        return ProjectBlockSummary(block.id)
    }

}
