/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain

import uk.gov.london.ops.domain.Requirement
import uk.gov.london.ops.framework.JSONUtils
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.PostLoad
import javax.persistence.Transient

/**
 *
 * Created by carmina on 25/11/2019.
 */
@Entity
@DiscriminatorValue("USER_DEFINED_OUTPUTS")
class UserDefinedOutputTemplateBlock : TemplateBlock() {

    @Transient
    var definedOutputTextSingular = "User Defined Output"

    @Transient
    var definedOutputTextPlural = "User Defined Outputs"

    @Transient
    var minNumberOfEntities: Int? = null

    @Transient
    var maxNumberOfEntities: Int? = null

    @Transient
    var outputNameText: String? = null

    @Transient
    var outputNameCharacterLimit: String? = null

    @Transient
    var amountToDeliverText: String? = null

    @Transient
    var amountToDeliverCharacterLimit: String? = null

    @Transient
    var baselineText: String? = null

    @Transient
    var baselineCharacterLimit: String? = null

    @Transient
    var baselineRequirement = Requirement.optional

    @Transient
    var monitorQuestion: String? = null

    @Transient
    var monitorQuestionCharacterLimit: String? = null

    @PostLoad
    fun loadBlockData() {
        val data = JSONUtils.fromJSON(this.blockData, UserDefinedOutputTemplateBlock::class.java)
        if (data != null) {
            this.definedOutputTextSingular = data.definedOutputTextSingular
            this.definedOutputTextPlural = data.definedOutputTextPlural
            this.minNumberOfEntities = data.minNumberOfEntities
            this.maxNumberOfEntities = data.maxNumberOfEntities
            this.outputNameText = data.outputNameText
            this.outputNameCharacterLimit = data.outputNameCharacterLimit
            this.amountToDeliverText = data.amountToDeliverText
            this.amountToDeliverCharacterLimit = data.amountToDeliverCharacterLimit
            this.baselineText = data.baselineText
            this.baselineRequirement = data.baselineRequirement
            this.baselineCharacterLimit = data.baselineCharacterLimit
            this.monitorQuestion = data.monitorQuestion
            this.monitorQuestionCharacterLimit = data.monitorQuestionCharacterLimit
        }
    }

    override fun updateCloneFromBlock(clone: TemplateBlock) {
        super.updateCloneFromBlock(clone)
        val cloned = clone as UserDefinedOutputTemplateBlock
        cloned.definedOutputTextSingular = definedOutputTextSingular
        cloned.definedOutputTextPlural = definedOutputTextPlural
        cloned.minNumberOfEntities = minNumberOfEntities
        cloned.maxNumberOfEntities = maxNumberOfEntities
        cloned.outputNameText = outputNameText
        cloned.outputNameCharacterLimit = outputNameCharacterLimit
        cloned.amountToDeliverText = amountToDeliverText
        cloned.amountToDeliverCharacterLimit = amountToDeliverCharacterLimit
        cloned.baselineText = baselineText
        cloned.baselineRequirement = baselineRequirement
        cloned.baselineCharacterLimit = baselineCharacterLimit
        cloned.monitorQuestion = monitorQuestion
        cloned.monitorQuestionCharacterLimit = monitorQuestionCharacterLimit
    }

    override fun shouldSaveBlockData(): Boolean {
        return true
    }

}
