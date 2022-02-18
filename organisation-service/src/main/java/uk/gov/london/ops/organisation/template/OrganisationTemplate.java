/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.template;

import java.util.ArrayList;
import java.util.List;

/**
 * An organisation's template.
 */
public class OrganisationTemplate {
    private Integer id;
    private String name;
    private String description;
    private Double displayOrder;
    private List<OrganisationBlockTemplate> blocks = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<OrganisationBlockTemplate> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<OrganisationBlockTemplate> blocks) {
        this.blocks = blocks;
    }

    public OrganisationBlockQuestionTemplate getQuestion(String fieldName) {
        for (OrganisationBlockTemplate block : getBlocks()) {
            for (OrganisationBlockQuestionTemplate question : block.getQuestions()) {
                if (question.getModelAttribute().equals(fieldName)) {
                    return question;
                }
            }
        }
        return null;
    }
}
