/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity

import org.springframework.util.StringUtils
import uk.gov.london.ops.framework.jpa.Join
import uk.gov.london.ops.framework.jpa.JoinData
import uk.gov.london.ops.framework.ComparableItem
import uk.gov.london.ops.project.block.ProjectDifference
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * Created by carmina on 02/12/2019.
 */
@Entity(name = "project_element")
class ProjectElement : RepeatingEntity, ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_element_seq_gen")
    @SequenceGenerator(name = "project_element_seq_gen", sequenceName = "project_element_seq    ", initialValue = 10000, allocationSize = 1)
    private var id: Int? = null

    @Column(name = "name")
    var name: String? = null

    @Column(name = "postcode")
    var postcode: String? = null

    @Column(name = "project_type")
    var projectType: String? = null

    @Column(name = "guidance")
    var guidance: String? = null

    @Column(name = "description")
    var description: String? = null

    @Column(name = "project_classification")
    var projectClassification: String? = null

    @Column(name = "project_stage")
    var projectStage: String? = null

    @Column(name = "operational_period")
    var operationalPeriod: String? = null

    @Column
    private var createdBy: String? = null

    @Column
    private var createdOn: OffsetDateTime? = null

    @JoinColumn(name = "modified_by")
    private var modifiedBy: String? = null

    @Column
    private var modifiedOn: OffsetDateTime? = null

    @Column(name = "original_id")
    @JoinData(targetTable = "project_element", targetColumn = "id", joinType = Join.JoinType.OneToOne, comment = "A reference to a previous version of this item if this is a cloned via a new block version.")
    private var originalId: Int? = null

    constructor()

    constructor(name: String, postcode: String) {
        this.name = name
        this.name = name
    }


    override fun getId(): Int? {
        return id
    }

    override fun update(fromEntity: RepeatingEntity) {
        val fromPO = fromEntity as ProjectElement
        this.name = fromPO.name
        this.postcode = fromPO.postcode
        this.description = fromPO.description
        this.guidance = fromPO.guidance
        this.projectType = fromPO.projectType
        this.projectClassification = fromPO.projectClassification
        this.projectStage = fromPO.projectStage
        this.operationalPeriod = fromPO.operationalPeriod
    }

    override fun getCreatedOn(): OffsetDateTime? {
        return createdOn
    }

    override fun setCreatedOn(createdOn: OffsetDateTime?) {
        this.createdOn = createdOn
    }


    override fun getModifiedOn(): OffsetDateTime? {
        return modifiedOn
    }

    override fun setModifiedOn(modifiedOn: OffsetDateTime) {
        this.modifiedOn = modifiedOn
    }

    fun setId(id: Int?) {
        this.id = id
    }

    override fun getOriginalId(): Int? {
        return if (originalId == null) {
            id
        } else originalId
    }

    override fun getCreatedBy(): String? {
        return createdBy
    }

    override fun setCreatedBy(createdBy: String?) {
        this.createdBy = createdBy
    }

    fun setOriginalId(originalId: Int?) {
        this.originalId = originalId
    }

    override fun isComplete(): Boolean {
        return !(StringUtils.isEmpty(name) || StringUtils.isEmpty(postcode))
    }

    override fun copy(): ProjectElement {
        val copy = ProjectElement()
        copy.name = name
        copy.postcode = postcode
        copy.description = description
        copy.guidance = guidance
        copy.projectType = projectType
        copy.projectClassification = projectClassification
        copy.projectStage = projectStage
        copy.operationalPeriod = operationalPeriod
        copy.setOriginalId(getOriginalId())
        copy.setCreatedBy(getCreatedBy())
        copy.setCreatedOn(getCreatedOn())
        return copy
    }

    override fun getModifiedBy(): String? {
        return modifiedBy
    }

    override fun setModifiedBy(modifiedBy: String) {
        this.modifiedBy = modifiedBy
    }

    override fun getComparisonId(): String {
        return getOriginalId().toString()
    }

    internal fun compareWith(projectElement: ProjectElement): List<ProjectDifference> {
        val differences = ArrayList<ProjectDifference>()

        if (StringUtils.trimAllWhitespace(this.name!!) != StringUtils.trimAllWhitespace(projectElement.name!!)) {
            differences.add(ProjectDifference(this, "name"))
        }

        if (StringUtils.trimAllWhitespace(this.postcode!!) != StringUtils.trimAllWhitespace(projectElement.postcode!!)) {
            differences.add(ProjectDifference(this, "postcode"))
        }

        if (StringUtils.trimAllWhitespace(this.projectType!!) != StringUtils.trimAllWhitespace(projectElement.projectType!!)) {
            differences.add(ProjectDifference(this, "projectType"))
        }

        if (StringUtils.trimAllWhitespace(this.guidance!!) != StringUtils.trimAllWhitespace(projectElement.guidance!!)) {
            differences.add(ProjectDifference(this, "guidance"))
        }

        if (StringUtils.trimAllWhitespace(this.description!!) != StringUtils.trimAllWhitespace(projectElement.description!!)) {
            differences.add(ProjectDifference(this, "description"))
        }

        if (StringUtils.trimAllWhitespace(this.projectClassification!!) != StringUtils.trimAllWhitespace(projectElement.projectClassification!!)) {
            differences.add(ProjectDifference(this, "projectDescription"))
        }

        if (StringUtils.trimAllWhitespace(this.projectStage!!) != StringUtils.trimAllWhitespace(projectElement.projectStage!!)) {
            differences.add(ProjectDifference(this, "projectStage"))
        }

        if (StringUtils.trimAllWhitespace(this.operationalPeriod!!) != StringUtils.trimAllWhitespace(projectElement.operationalPeriod!!)) {
            differences.add(ProjectDifference(this, "operationalPeriod"))
        }

        return differences
    }

}
