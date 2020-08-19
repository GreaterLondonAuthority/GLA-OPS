/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment

import javax.persistence.*

/**
 * Created by cmatias on 10/09/2019.
 */
@Entity(name = "criteria_answer_options")
class CriteriaAnswerOption {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "criteria_answer_option_seq_gen")
        @SequenceGenerator(name = "criteria_answer_option_seq_gen", sequenceName = "criteria_answer_option_seq", initialValue = 100, allocationSize = 1)
        var id: Int? = null

        var title: String

        var displayOrder: Double

        constructor(title: String, displayOrder: Double) {
                this.title = title
                this.displayOrder = displayOrder
        }
}