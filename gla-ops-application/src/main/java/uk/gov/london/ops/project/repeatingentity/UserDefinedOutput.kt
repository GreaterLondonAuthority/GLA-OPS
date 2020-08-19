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
 * Created by carmina on 25/11/2019.
 */
@Entity(name = "user_defined_output")
class UserDefinedOutput : RepeatingEntity, ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_defined_output_seq_gen")
    @SequenceGenerator(name = "user_defined_output_seq_gen", sequenceName = "user_defined_output_seq    ", initialValue = 10000, allocationSize = 1)
    private var id: Int? = null

    @Column(name = "output_name")
    var outputName: String? = null

    @Column(name = "delivery_amount")
    var deliveryAmount: String? = null

    @Column(name = "baseline")
    var baseline: String? = null

    @Column(name = "monitor_of_output")
    var monitorOfOutput: String? = null

    @Column
    private var createdBy: String? = null

    @Column
    private var createdOn: OffsetDateTime? = null

    @JoinColumn(name = "modified_by")
    private var modifiedBy: String? = null

    @Column
    private var modifiedOn: OffsetDateTime? = null

    @Column(name = "original_id")
    @JoinData(targetTable = "user_defined_output", targetColumn = "id", joinType = Join.JoinType.OneToOne, comment = "A reference to a previous version of this item if this is a cloned via a new block version.")
    private var originalId: Int? = null

    constructor()

    constructor(outputName: String, deliveryAmount: String, baseline: String, monitorOfOutput: String) {
        this.outputName = outputName
        this.deliveryAmount = deliveryAmount
        this.baseline = baseline
        this.monitorOfOutput = monitorOfOutput
    }


    override fun getId(): Int? {
        return id
    }

    override fun update(fromEntity: RepeatingEntity) {
        val fromPO = fromEntity as UserDefinedOutput
        this.outputName = fromPO.outputName
        this.deliveryAmount = fromPO.deliveryAmount
        this.baseline = fromPO.baseline
        this.monitorOfOutput = fromPO.monitorOfOutput
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
        return !(StringUtils.isEmpty(outputName) || StringUtils.isEmpty(deliveryAmount) || StringUtils.isEmpty(monitorOfOutput))
    }

    override fun copy(): UserDefinedOutput {
        val copy = UserDefinedOutput()
        copy.outputName = outputName
        copy.deliveryAmount = deliveryAmount
        copy.baseline = baseline
        copy.monitorOfOutput = monitorOfOutput
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

    internal fun compareWith(projectObjective: UserDefinedOutput): List<ProjectDifference> {
        val differences = ArrayList<ProjectDifference>()

        if (StringUtils.trimAllWhitespace(this.outputName!!) != StringUtils.trimAllWhitespace(projectObjective.outputName!!)) {
            differences.add(ProjectDifference(this, "outputName"))
        }

        if (StringUtils.trimAllWhitespace(this.deliveryAmount!!) != StringUtils.trimAllWhitespace(projectObjective.deliveryAmount!!)) {
            differences.add(ProjectDifference(this, "deliveryAmount"))
        }

        if (StringUtils.trimAllWhitespace(this.baseline!!) != StringUtils.trimAllWhitespace(projectObjective.baseline!!)) {
            differences.add(ProjectDifference(this, "baseline"))
        }

        if (StringUtils.trimAllWhitespace(this.monitorOfOutput!!) != StringUtils.trimAllWhitespace(projectObjective.monitorOfOutput!!)) {
            differences.add(ProjectDifference(this, "monitorOfOutput"))
        }

        return differences
    }

}
