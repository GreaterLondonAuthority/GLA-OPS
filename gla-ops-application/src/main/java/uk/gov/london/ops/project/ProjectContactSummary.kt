package uk.gov.london.ops.project

class ProjectContactSummary(
    val id: Int? = null,
    val projectName: String? = null,
    val contactEmail: String? = null,
    val contactName: String? = null,
    val orgId: Int? = null,
    val orgName: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectContactSummary

        if (id != other.id) return false
        if (contactEmail != other.contactEmail) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (contactEmail?.hashCode() ?: 0)
        return result
    }


}
