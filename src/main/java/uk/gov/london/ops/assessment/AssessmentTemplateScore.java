/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name="assessment_template_score")
public class AssessmentTemplateScore {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_template_score_seq_gen")
    @SequenceGenerator(name = "assessment_template_score_seq_gen", sequenceName = "assessment_template_score_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column
    private BigDecimal score;

    @Column
    private String name;

    @Column
    private String description;

    public AssessmentTemplateScore() {}

    public AssessmentTemplateScore(BigDecimal score, String name, String description) {
        this.score = score;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
