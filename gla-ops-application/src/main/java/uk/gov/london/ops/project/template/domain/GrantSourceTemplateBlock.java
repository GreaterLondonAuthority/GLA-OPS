/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import static uk.gov.london.ops.payment.PaymentSource.DPF;
import static uk.gov.london.ops.payment.PaymentSource.GRANT;
import static uk.gov.london.ops.payment.PaymentSource.RCGF;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import uk.gov.london.ops.project.block.ProjectBlockType;

@Entity
@DiscriminatorValue("GRANT_SOURCE")
public class GrantSourceTemplateBlock extends TemplateBlock {

    static final Set<String> all_grant_types = new HashSet(Arrays.asList(GRANT, DPF, RCGF));

    @Column(name = "nil_grant_hidden")
    private boolean nilGrantHidden;

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


    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        GrantSourceTemplateBlock gsb = (GrantSourceTemplateBlock) clone;
        gsb.setNilGrantHidden(this.isNilGrantHidden());
        gsb.setDescription(this.getDescription());
        gsb.setGrantTotalText(this.getGrantTotalText());

    }
}
