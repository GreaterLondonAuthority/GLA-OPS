/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.risk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;
import uk.gov.london.ops.project.template.domain.InternalRiskTemplateBlock;
import uk.gov.london.ops.project.template.domain.InternalTemplateBlock;
import uk.gov.london.ops.project.template.domain.RiskRating;

@Entity(name = "internal_risk_block")
@DiscriminatorValue("RISK")
@JoinData(sourceTable = "internal_risks_block", sourceColumn = "id", targetTable = "internal_project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the risks block is a subclass of the internal project block and shares a common key")
public class InternalRiskBlock extends InternalProjectBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "internal_risk_block_seq_gen")
    @SequenceGenerator(name = "internal_risk_block_seq_gen", sequenceName = "internal_risk_block_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "rating_id")
    private RiskRating rating;

    @Column(name = "risk_adjusted_figures_flag")
    private boolean riskAdjustedFiguresFlag = false;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = RiskAdjustedFigures.class, orphanRemoval = true)
    @JoinColumn(name = "block_id")
    private List<RiskAdjustedFigures> riskAdjustedFiguresList = new ArrayList<>();

    @Override
    public Integer getId() {
        return id;
    }

    public RiskRating getRating() {
        return rating;
    }

    public void setRating(RiskRating rating) {
        this.rating = rating;
    }

    public List<RiskAdjustedFigures> getRiskAdjustedFiguresList() {
        return riskAdjustedFiguresList;
    }


    public void setRiskAdjustedFiguresList(
            List<RiskAdjustedFigures> riskAdjustedFiguresList) {
        this.riskAdjustedFiguresList = riskAdjustedFiguresList;
    }

    public boolean getRiskAdjustedFiguresFlag() {
        return riskAdjustedFiguresFlag;
    }

    public void setRiskAdjustedFiguresFlag(boolean riskAdjustedFiguresFlag) {
        this.riskAdjustedFiguresFlag = riskAdjustedFiguresFlag;
    }

    @Override
    public InternalRiskBlock clone() {
        InternalRiskBlock clone = (InternalRiskBlock) super.clone();
        clone.setRating(this.getRating());
        clone.setRiskAdjustedFiguresFlag(this.getRiskAdjustedFiguresFlag());

        for (RiskAdjustedFigures riskAdjustedFigures : riskAdjustedFiguresList) {
            clone.getRiskAdjustedFiguresList().add(riskAdjustedFigures.clone());
        }

        return clone;
    }

    @JsonIgnore
    private String getRatingName() {
        return this.getRating() != null ? this.getRating().getName() : "NOT_SET";
    }

    @Override
    public String merge(InternalProjectBlock updated) {
        InternalRiskBlock other = (InternalRiskBlock) updated;
        String auditMessage = "changed rating from " + this.getRatingName() + " to " + other.getRatingName();
        this.setRating(other.getRating());

        if (other.getRiskAdjustedFiguresList() != null && !other.getRiskAdjustedFiguresList().isEmpty()) {
            this.riskAdjustedFiguresList.clear();
            this.riskAdjustedFiguresList.addAll(other.getRiskAdjustedFiguresList());
        }

        return auditMessage;
    }

    @Override
    protected void initFromTemplateSpecific(InternalTemplateBlock templateBlock) {
        if (templateBlock instanceof InternalRiskTemplateBlock) {
            this.setRiskAdjustedFiguresFlag(((InternalRiskTemplateBlock) templateBlock).getRiskAdjustedFiguresFlag());
        }
    }

}
