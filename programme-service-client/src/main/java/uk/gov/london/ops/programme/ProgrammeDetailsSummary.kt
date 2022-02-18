package uk.gov.london.ops.programme

data class ProgrammeDetailsSummary(
        val id: Int? = null,
        val name: String? = null,
        val managingOrganisationId: Int? = null,
        var companyEmail: String? = null,
        val grantTypes: Set<String>? = null,
        val hasIndicativeTemplate: Boolean = false
)
