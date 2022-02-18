/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain

import uk.gov.london.ops.framework.JSONUtils
import uk.gov.london.ops.project.block.ProjectBlockType
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.PostLoad
import javax.persistence.Transient

@Entity
@DiscriminatorValue("UNIT_DETAILS")
class UnitDetailsTemplateBlock : TemplateBlock(ProjectBlockType.UnitDetails) {

    @Transient
    var buildTypeOfWhichCategories: List<String>? = null

    @Transient
    var hideWheelChairSection: Boolean = false


    @PostLoad
    fun loadBlockData() {
        val data = JSONUtils.fromJSON(this.blockData, UnitDetailsTemplateBlock::class.java)
        if (data != null) {
            this.buildTypeOfWhichCategories = data.buildTypeOfWhichCategories
            this.hideWheelChairSection = data.hideWheelChairSection
        }
    }

    override fun updateCloneFromBlock(clone: TemplateBlock) {
        super.updateCloneFromBlock(clone)
        val cloned = clone as UnitDetailsTemplateBlock
        cloned.buildTypeOfWhichCategories = buildTypeOfWhichCategories
        cloned.hideWheelChairSection = hideWheelChairSection
    }

    override fun shouldSaveBlockData(): Boolean {
        return true
    }

}
