/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.domain.Requirement;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "assessment_section")
public class AssessmentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_section_seq_gen")
    @SequenceGenerator(name = "assessment_section_seq_gen", sequenceName = "assessment_section_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "assessment_template_section_id")
    private AssessmentTemplateSection sectionTemplate;

    @Column(name = "comments")
    private String comments;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentCriteria.class)
    @JoinColumn(name = "assessment_section_id")
    private List<AssessmentCriteria> criteriaList = new ArrayList<>();

    public AssessmentSection() {}

    public AssessmentSection(AssessmentTemplateSection sectionTemplate) {
        this.sectionTemplate = sectionTemplate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AssessmentTemplateSection getSectionTemplate() {
        return sectionTemplate;
    }

    public void setSectionTemplate(AssessmentTemplateSection sectionTemplate) {
        this.sectionTemplate = sectionTemplate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<AssessmentCriteria> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<AssessmentCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getTitle() {
        return sectionTemplate != null ? sectionTemplate.getTitle() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Double getDisplayOrder() {
        return sectionTemplate != null ? sectionTemplate.getDisplayOrder() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getWeight() {
        return sectionTemplate != null ? sectionTemplate.getWeight() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Requirement getCommentsRequirement() {
        return sectionTemplate != null ? sectionTemplate.getCommentsRequirement() : null;
    }

    public void merge(AssessmentSection updated) {
        this.setComments(updated.getComments());
        for (AssessmentCriteria criteria: updated.getCriteriaList()) {
            this.getCriteria(criteria.getId()).merge(criteria);
        }
    }

    private AssessmentCriteria getCriteria(Integer id) {
        return criteriaList.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

}
