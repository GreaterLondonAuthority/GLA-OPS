package uk.gov.london.ops.project.template.domain

import uk.gov.london.ops.framework.enums.Requirement

interface Commandable {
    fun performCommand(command: TemplateBlockCommand, payload: CommandPayload?)

    fun getTemplateBlockCommands(): List<TemplateBlockCommand?>?

    fun getId(): Integer
}

class CommandPayload (
    var blockData: BlockData?
)

class BlockData (
        var blockId: Int?,
        var blockType: String?,
        var blockOldName: String?,
        var blockNewName: String?,
        var infoMessage: String?,
        var questionId: Integer?,
        var processingRouteId: Integer?,
        var milestoneExternalId: Integer?,
        var milestoneSummary: String?,
        var milestoneDisplayOrder: Integer?,
        var milestoneRequirement: Requirement?,
        var milestoneNaSelectable: Boolean?,
        var learningGrantLabels: LearningGrantLabels?,
        var userDefinedOutputTemplateBlock: UserDefinedOutputTemplateBlock?
)

class LearningGrantLabels (
        var profileTitle: String?,
        var allocationTitle: String?,
        var cumulativeAllocationTitle: String?,
        var cumulativeEarningsTitle: String?,
        var cumulativePaymentTitle: String?,
        var paymentDueTitle: String?
)
