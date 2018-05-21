/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.project.ProjectBlockType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;

import static uk.gov.london.ops.util.GlaOpsUtils.listToCsString;

@Entity
@DiscriminatorValue("GRANT_SOURCE")
public class GrantSourceTemplateBlock extends TemplateBlock {

    static final List<String> all_grant_types = Arrays.asList("Grant", "DPF", "RCGF");

    @Column(name = "nil_grant_hidden")
    private boolean nilGrantHidden;

    @Column(name = "grant_types")
    private String grantTypesString;

    @Column(name = "grant_description")
    private String description;

    @Column(name = "grant_total_text")
    private String grantTotalText;

    public GrantSourceTemplateBlock() {
        super(ProjectBlockType.GrantSource);
    }

    public GrantSourceTemplateBlock(int displayOrder) {
        super(displayOrder, ProjectBlockType.GrantSource);
    }

    public boolean isNilGrantHidden() {
        return nilGrantHidden;
    }

    public void setNilGrantHidden(boolean nilGrantHidden) {
        this.nilGrantHidden = nilGrantHidden;
    }

    public String getGrantTypesString() {
        return grantTypesString;
    }

    public void setGrantTypesString(String grantTypesString) {
        this.grantTypesString = grantTypesString;
    }

    public List<String> getGrantTypes() {
        if (grantTypesString == null || grantTypesString.isEmpty()) {
            return all_grant_types;
        }
        else {
            return Arrays.asList(grantTypesString.split(","));
        }
    }

    public void setGrantTypes(List<String> grantTypes) {
        this.grantTypesString = listToCsString(grantTypes);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGrantTotalText() {
        return grantTotalText;
    }

    public void setGrantTotalText(String grantTotalText) {
        this.grantTotalText = grantTotalText;
    }

}
