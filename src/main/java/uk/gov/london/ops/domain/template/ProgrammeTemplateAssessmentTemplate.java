/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.assessment.AssessmentTemplate;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cmatias on 18/12/2018.
 */

@Entity(name = "ProgrammeTemplateAssessmentTemplate")
@Table(name = "programme_template_assessment_template")
public class ProgrammeTemplateAssessmentTemplate implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ptat_seq_gen")
    @SequenceGenerator(name = "ptat_seq_gen", sequenceName = "ptat_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @Column(name = "programme_id")
    private Integer programme;

    @JsonIgnore
    @Column(name = "template_id")
    private Integer template;

    @JoinData(sourceTable = "programme_template_assessment_template", sourceColumn = "assessment_template_id", targetTable = "assessment_template", targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "part of compound primary key.")
    @ManyToOne
    @JoinColumn(name = "assessment_template_id")
    private AssessmentTemplate assessmentTemplate;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "ptat_roles", joinColumns = @JoinColumn(name = "ptat_id"))
    @Column(name = "role")
    private Set<String> allowedRoles = new HashSet<>();


    @Transient
    private boolean isUsedInAssessment;


    public ProgrammeTemplateAssessmentTemplate() {
    }

    public ProgrammeTemplateAssessmentTemplate(Integer programme, Integer template, AssessmentTemplate assessmentTemplate) {
        this.programme = programme;
        this.template = template;
        this.assessmentTemplate = assessmentTemplate;
    }

    public Integer getProgramme() {
        return programme;
    }

    public void setProgramme(Integer programme) {
        this.programme = programme;
    }

    public Integer getTemplate() {
        return template;
    }

    public void setTemplate(Integer template) {
        this.template = template;
    }

    public AssessmentTemplate getAssessmentTemplate() {
        return assessmentTemplate;
    }

    public void setAssessmentTemplate(AssessmentTemplate assessmentTemplate) {
        this.assessmentTemplate = assessmentTemplate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isUsedInAssessment() {
        return isUsedInAssessment;
    }

    public void setUsedInAssessment(boolean usedInAssessment) {
        isUsedInAssessment = usedInAssessment;
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Set<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }
}