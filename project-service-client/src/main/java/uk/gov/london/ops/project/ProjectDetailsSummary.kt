package uk.gov.london.ops.project

class ProjectDetailsSummary(
        val projectId: Int? = null,
        var programmeId: Int? = null,
        var templateId: Int? = null,
        var managingOrganisationId: Int? = null,
        var statusName: String? = null,
        var subStatusName: String? = null
)
