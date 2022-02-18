/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.assessment;

import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.enums.Requirement;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "assessment_template_section")
public class AssessmentTemplateSection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assessment_template_section_seq_gen")
    @SequenceGenerator(name = "assessment_template_section_seq_gen", sequenceName = "assessment_template_section_seq",
            initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "comments_requirement")
    @Enumerated(EnumType.STRING)
    private Requirement commentsRequirement;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentTemplateCriteria.class)
    @JoinColumn(name = "assessment_template_section_id")
    private List<AssessmentTemplateCriteria> criteriaList = new ArrayList<>();

    @Column(name = "display_order")
    private Double displayOrder;

    public AssessmentTemplateSection() {}

    public AssessmentTemplateSection(String title, Requirement commentsRequirement, Double displayOrder) {
        this.title = title;
        this.commentsRequirement = commentsRequirement;
        this.displayOrder = displayOrder;
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
        return criteriaList.stream()
                .map(AssessmentTemplateCriteria::getWeight)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue).sum();
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
        if (criteriaList != null && !criteriaList.isEmpty()) {
            return criteriaList.stream().map(AssessmentTemplateCriteria::getWeight).reduce(0, GlaUtils::nullSafeAdd);
        }
        return 0;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        if (displayOrder == null) {
            this.displayOrder = new Double(id);
        } else {
            this.displayOrder = displayOrder;
        }
    }
}
