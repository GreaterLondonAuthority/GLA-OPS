package uk.gov.london.ops.project.template.domain

data class BlockUsage(val templateId: Int,
                      val templateName: String,
                      val programmeId: Int,
                      val programmeName: String,
                      val managingOrganisation: String)
