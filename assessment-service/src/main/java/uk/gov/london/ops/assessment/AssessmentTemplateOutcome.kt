/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment

import javax.persistence.*

enum class AllowToProceed {
    Proceed,
    DoNotProceed,
    ForInformationOnly
}

@Entity(name = "assessment_template_outcome")
class AssessmentTemplateOutcome (

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_template_outcome_seq_gen")
        @SequenceGenerator(name = "assessment_template_outcome_seq_gen", sequenceName = "assessment_template_outcome_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null,

        var name: String,

        var allowToProceed: AllowToProceed = AllowToProceed.ForInformationOnly,

        var displayOrder: Double

)
