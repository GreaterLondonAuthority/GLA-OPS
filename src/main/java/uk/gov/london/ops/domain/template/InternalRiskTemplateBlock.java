/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import uk.gov.london.ops.domain.project.InternalBlockType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("RISK")
public class InternalRiskTemplateBlock extends InternalTemplateBlock {

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = RiskRating.class)
    @JoinColumn(name = "block_id")
    private List<RiskRating> ratingList = new ArrayList<>();

    @Column(name = "risk_adjusted_figures_flag")
    private boolean riskAdjustedFiguresFlag = false;

    public InternalRiskTemplateBlock() {
        super(InternalBlockType.Risk);
    }

    public List<RiskRating> getRatingList() {
        return ratingList;
    }

    public void setRatingList(List<RiskRating> ratingList) {
        this.ratingList = ratingList;
    }

    public boolean getRiskAdjustedFiguresFlag() {
        return riskAdjustedFiguresFlag;
    }

    public void setRiskAdjustedFiguresFlag(boolean riskAdjustedFiguresFlag) {
        this.riskAdjustedFiguresFlag = riskAdjustedFiguresFlag;
    }

    public RiskRating getRating(Integer id) {
        return ratingList.stream().filter(rr -> id.equals(rr.getId())).findFirst().orElse(null);
    }

    @Override
    public InternalRiskTemplateBlock clone() {
        InternalRiskTemplateBlock clone = (InternalRiskTemplateBlock) super.clone();
        for (RiskRating riskRating: ratingList) {
            clone.getRatingList().add(riskRating.clone());
        }

        clone.setRiskAdjustedFiguresFlag(getRiskAdjustedFiguresFlag());

        return clone;
    }

}
