/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import org.apache.commons.collections.CollectionUtils;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.domain.Requirement;
import uk.gov.london.common.error.ApiErrorItem;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(name="assessment_template_section")
public class AssessmentTemplateSection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_template_section_seq_gen")
    @SequenceGenerator(name = "assessment_template_section_seq_gen", sequenceName = "assessment_template_section_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "comments_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement commentsRequirement;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentTemplateCriteria.class)
    @JoinColumn(name = "assessment_template_section_id")
    private List<AssessmentTemplateCriteria> criteriaList = new ArrayList<>();

    public AssessmentTemplateSection() {}

    public AssessmentTemplateSection(String title, Integer weight, Requirement commentsRequirement) {
        this.title = title;
        this.weight = weight;
        this.commentsRequirement = commentsRequirement;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Requirement getCommentsRequirement() {
        return commentsRequirement;
    }

    public void setCommentsRequirement(Requirement commentsRequirement) {
        this.commentsRequirement = commentsRequirement;
    }

    public List<AssessmentTemplateCriteria> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<AssessmentTemplateCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    public int getTotalCriteriaWeight() {
        if (CollectionUtils.isNotEmpty(criteriaList)) {
            return criteriaList.stream().map(c -> c.getWeight()).reduce(0, GlaUtils::nullSafeAdd);
        }
        return 0;
    }

    public Map<String, List<ApiErrorItem>> getValidationFailures() {
        Map<String, List<ApiErrorItem>> errors = new HashMap<>();
        if (CollectionUtils.isNotEmpty(criteriaList) && getTotalCriteriaWeight() > 0 && getTotalCriteriaWeight() != 100) {
            errors.put("criteria", new ArrayList<>());
            errors.get("criteria").add(new ApiErrorItem("Total weight percentage for criteria in the section must total 100%"));
        }
        return errors;
    }

}
