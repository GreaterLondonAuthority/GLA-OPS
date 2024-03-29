/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import uk.gov.london.ops.framework.JSONUtils;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.block.ProjectBlockType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("INDICATIVE_GRANT")
public class IndicativeGrantTemplateBlock extends BaseGrantTemplateBlock {

    @JoinData(joinType = Join.JoinType.OneToMany, sourceTable = "template_block", sourceColumn = "id",
            targetColumn = "template_block_id", targetTable = "indicative_tenure_config_year", comment = "")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = TenureYear.class)
    @JoinColumn(name = "template_block_id")
    private Set<TenureYear> tenureYears = new HashSet<>();

    @Column(name = "allow_zero_indicative_units")
    boolean allowZeroUnits;


    public IndicativeGrantTemplateBlock() {
        super(ProjectBlockType.IndicativeGrant);
    }

    public Set<TenureYear> getTenureYears() {
        return tenureYears;
    }

    public void setTenureYears(Set<TenureYear> tenureYears) {
        this.tenureYears = tenureYears;
    }

    public boolean isAllowZeroUnits() {
        return allowZeroUnits;
    }

    public void setAllowZeroUnits(boolean allowZeroUnits) {
        this.allowZeroUnits = allowZeroUnits;
    }

    @Override
    public void updateCloneFromBlock(TemplateBlock clone) {
        IndicativeGrantTemplateBlock block = (IndicativeGrantTemplateBlock) clone;
        block.setAllowZeroUnits(this.isAllowZeroUnits());
        block.setOtherAffordableTenureTypes(this.getOtherAffordableTenureTypes());
        block.setShowOtherAffordableQuestion(this.isShowOtherAffordableQuestion());
        for (TenureYear tenureYear : this.getTenureYears()) {
            TenureYear newYear = new TenureYear(tenureYear.getYear(), tenureYear.getExternalId(), tenureYear.getTariffRate());
            block.getTenureYears().add(newYear);
        }
    }
    @PostLoad
    void loadBlockData() {
        IndicativeGrantTemplateBlock data = JSONUtils.fromJSON(this.blockData,
                IndicativeGrantTemplateBlock.class);
        if (data != null) {
            this.setOtherAffordableTenureTypes(data.getOtherAffordableTenureTypes());
            this.setShowOtherAffordableQuestion(data.isShowOtherAffordableQuestion());
        }
    }


}
