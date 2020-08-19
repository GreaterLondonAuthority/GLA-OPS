/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template;

import uk.gov.london.ops.project.block.ProjectBlockType;

public class ProjectBlockTypeModel {

    private ProjectBlockType blockType;
    private String displayName;
    private String templateClassName;

    public ProjectBlockTypeModel() {}

    public ProjectBlockTypeModel(ProjectBlockType blockType, String displayName, String templateClassName) {
        this.blockType = blockType;
        this.displayName = displayName;
        this.templateClassName = templateClassName;
    }

    public ProjectBlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(ProjectBlockType blockType) {
        this.blockType = blockType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTemplateClassName() {
        return templateClassName;
    }

    public void setTemplateClassName(String templateClassName) {
        this.templateClassName = templateClassName;
    }

}
