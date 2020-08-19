/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain

import uk.gov.london.ops.framework.JSONUtils
import java.util.*
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.PostLoad
import javax.persistence.Transient

/**
 *
 * Created by carmina on 02/12/2019.
 */
@Entity
@DiscriminatorValue("PROJECTELEMENTS")
class ProjectElementsTemplateBlock : RepeatingEntityTemplateBlock() {

    @Transient
    var elementTextSingular = "Element"

    @Transient
    var elementTextPlural = "Elements"

    @Transient
    var projectTypesText: String? = null

    @Transient
    var projectTypes: Set<AnswerOption> = HashSet()

    @Transient
    var guidanceText: String? = null

    @Transient
    var descriptionText: String? = null

    @Transient
    var projectClassificationText: String? = null

    @Transient
    var projectClassifications: Set<AnswerOption> = HashSet()

    @Transient
    var projectStagesText: String? = null

    @Transient
    var projectStages: Set<AnswerOption> = HashSet()

    @Transient
    var operationalPeriodText: String? = null

    override fun shouldSaveBlockData(): Boolean {
        return true
    }


    @PostLoad
    override fun loadBlockData() {
        super.loadBlockData()
        val data = JSONUtils.fromJSON(this.blockData, ProjectElementsTemplateBlock::class.java)
        if (data != null) {
            this.elementTextPlural = data.elementTextPlural
            this.elementTextSingular = data.elementTextSingular
            this.guidanceText = data.guidanceText
            this.descriptionText = data.descriptionText
            this.operationalPeriodText = data.operationalPeriodText

            this.projectTypesText = data.projectTypesText
            this.projectTypes = data.projectTypes
            this.projectClassificationText = data.projectClassificationText
            this.projectClassifications = data.projectClassifications
            this.projectStagesText = data.projectStagesText
            this.projectStages = data.projectStages

        }
    }


    override fun updateCloneFromBlock(clone: TemplateBlock) {
        super.updateCloneFromBlock(clone)
        val cloned = clone as ProjectElementsTemplateBlock
        cloned.elementTextPlural = elementTextPlural
        cloned.elementTextSingular = elementTextSingular
        cloned.minNumberOfEntities = minNumberOfEntities
        cloned.maxNumberOfEntities = maxNumberOfEntities
        cloned.guidanceText = guidanceText
        cloned.descriptionText = descriptionText
        cloned.operationalPeriodText = operationalPeriodText

        cloned.projectTypesText = projectTypesText
        cloned.projectTypes = projectTypes
        cloned.projectClassificationText = projectClassificationText
        cloned.projectClassifications = projectClassifications
        cloned.projectStagesText = projectStagesText
        cloned.projectStages = projectStages
    }

}
