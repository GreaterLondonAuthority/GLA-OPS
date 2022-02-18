/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.util.stream.Collectors;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.project.block.ProjectBlockType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.london.ops.refdata.PaymentSourceKt.*;

@Entity
@DiscriminatorValue("GRANT_SOURCE")
public class GrantSourceTemplateBlock extends TemplateBlock {

    static final Set<String> all_grant_types = new HashSet(Arrays.asList(GRANT, DPF, RCGF));

    @Column(name = "nil_grant_hidden")
    private boolean nilGrantHidden;

    @Transient
    private boolean showDescription = true;

    @Column(name = "grant_description")
    private String description = "Select and enter the amount from each grant source";

    @Transient
    private boolean showTotalDescription = true;

    @Column(name = "grant_total_text")
    private String grantTotalText = "You can request any amount up to";

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

    public Set<String> getGrantTypes() {
        Set<String> grantTypes = super.getGrantTypes();
        if (grantTypes.isEmpty()) {
            grantTypes = all_grant_types;
        }
        return grantTypes;
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

    public boolean isShowDescription() {
        return showDescription;
    }

    public void setShowDescription(boolean showDescription) {
        this.showDescription = showDescription;
    }

    public boolean isShowTotalDescription() {
        return showTotalDescription;
    }

    public void setShowTotalDescription(boolean showTotalDescription) {
        this.showTotalDescription = showTotalDescription;
    }

    @PostLoad
    void loadBlockData() {
        GrantSourceTemplateBlock data = JSONUtils.fromJSON(this.blockData, GrantSourceTemplateBlock.class);
        if (data != null) {
            this.setNilGrantHidden(data.isNilGrantHidden());
            this.setShowDescription(data.isShowDescription());
            this.setDescription(data.getDescription());
            this.setShowTotalDescription(data.isShowTotalDescription());
            this.setGrantTotalText(data.getGrantTotalText());
            this.setGrantTypes(data.getGrantTypes().stream().collect(Collectors.toList()));
        }
    }


    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        GrantSourceTemplateBlock gsb = (GrantSourceTemplateBlock) clone;
        gsb.setNilGrantHidden(this.isNilGrantHidden());
        gsb.setShowDescription(this.isShowDescription());
        gsb.setDescription(this.getDescription());
        gsb.setShowTotalDescription(this.isShowTotalDescription());
        gsb.setGrantTotalText(this.getGrantTotalText());
        gsb.setGrantTypes(this.getGrantTypes().stream().collect(Collectors.toList()));
    }
}
