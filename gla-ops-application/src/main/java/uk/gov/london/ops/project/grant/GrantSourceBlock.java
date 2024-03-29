/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.grant;

import uk.gov.london.ops.framework.enums.GrantType;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.ProjectInterface;
import uk.gov.london.ops.project.block.*;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.london.ops.framework.OPSUtils.toBigDecimal;

/**
 * Created by chris on 10/11/2016.
 */
@Entity(name = "grant_source_block")
@DiscriminatorValue("GRANT_SOURCE")
@JoinData(sourceTable = "grant_source_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the grant source block is a subclass of the project block and shares a common key")
public class GrantSourceBlock extends NamedProjectBlock implements FundingSourceProvider {

    @Column(name = "zero_grant_requested")
    private Boolean zeroGrantRequested;

    @Column(name = "associated_project")
    private boolean associatedProject;

    @Column(name = "grant_value")
    private Long grantValue;

    @Column(name = "rcgf_value")
    private Long recycledCapitalGrantFundValue;

    @Column(name = "dpf_value")
    private Long disposalProceedsFundValue;

    @Column(name = "strategic_funding")
    private Long strategicFunding;

    @Transient
    private boolean associatedProjectFlagUpdatable;

    public GrantSourceBlock() {
    }

    public GrantSourceBlock(Project project) {
        super(project);
    }

    public void merge(GrantSourceBlock other) {
        if (other != null) {
            this.zeroGrantRequested = other.isZeroGrantRequested();
            this.associatedProject = other.isAssociatedProject();
            this.strategicFunding = other.strategicFunding;

            // if zero requested then everything else is zero
            if (isZeroGrantRequested() || isAssociatedProject()) {
                this.grantValue = 0L;
                this.recycledCapitalGrantFundValue = 0L;
                this.disposalProceedsFundValue = 0L;
            } else {
                this.grantValue = other.getGrantValue();
                this.recycledCapitalGrantFundValue = other.getRecycledCapitalGrantFundValue();
                this.disposalProceedsFundValue = other.getDisposalProceedsFundValue();
            }
        } else {
            this.zeroGrantRequested = null;
            this.grantValue = null;
            this.recycledCapitalGrantFundValue = null;
            this.disposalProceedsFundValue = null;
        }
    }

    @Override
    @Transient
    protected void generateValidationFailures() {
        // must be at least £0 requested up to a maximum of Total Grant Eligibility

        if (isZeroGrantRequested()) {
            return;
        }

        if (associatedProject && strategicFunding == null) {
            this.addErrorMessage("Block1", "", "A value £ must be entered");
            return;
        }

        if (!associatedProject && (grantValue == null && recycledCapitalGrantFundValue == null
                && disposalProceedsFundValue == null)) {
            this.addErrorMessage("Block1", "", "At least one grant source must be entered.");
        }

        Long totalFundRequested = getTotalGrantRequested().longValue();

        if (this.getTotalGrantEligibility() != null) {
            if ((this.isAssociatedProject() && this.getStrategicFunding() > getTotalGrantEligibility()) || (totalFundRequested
                    > this.getTotalGrantEligibility())) {
                this.addErrorMessage("Block1", "", "The total amount requested must be less than the total grant available");
            }
        }
    }

    @Transient
    @Override
    public boolean isComplete() {
        return isVisited() && getValidationFailures().size() == 0;
    }

    public boolean isZeroGrantRequested() {
        return Boolean.TRUE.equals(zeroGrantRequested);
    }

    public void setZeroGrantRequested(Boolean zeroGrantRequested) {
        this.zeroGrantRequested = zeroGrantRequested;
    }

    public boolean isAssociatedProject() {
        return associatedProject;
    }

    public void setAssociatedProject(boolean associatedProject) {
        this.associatedProject = associatedProject;
    }

    public Long getGrantValue() {
        return grantValue;
    }

    public void setGrantValue(Long grantValue) {
        this.grantValue = grantValue;
    }

    public Long getRecycledCapitalGrantFundValue() {
        return recycledCapitalGrantFundValue;
    }

    public void setRecycledCapitalGrantFundValue(Long recycledCapitalGrantFundValue) {
        this.recycledCapitalGrantFundValue = recycledCapitalGrantFundValue;
    }

    public Long getDisposalProceedsFundValue() {
        return disposalProceedsFundValue;
    }

    public void setDisposalProceedsFundValue(Long disposalProceedsFundValue) {
        this.disposalProceedsFundValue = disposalProceedsFundValue;
    }

    public Long getStrategicFunding() {
        return strategicFunding;
    }

    public void setStrategicFunding(Long strategicFunding) {
        this.strategicFunding = strategicFunding;
    }

    public boolean isAssociatedProjectFlagUpdatable() {
        return associatedProjectFlagUpdatable;
    }

    public void setAssociatedProjectFlagUpdatable(boolean associatedProjectFlagUpdatable) {
        this.associatedProjectFlagUpdatable = associatedProjectFlagUpdatable;
    }

    @Transient
    public Long getTotalGrantEligibility() {
        ProjectInterface project = getProjectInterface();
        if (project != null) {
            return project.getTotalGrantEligibility();
        } else {
            return null;
        }
    }

    public BigDecimal getTotalGrantRequested() {
        if (isZeroGrantRequested() || isAssociatedProject()) {
            return BigDecimal.ZERO;
        }
        Long total = 0L;
        if (getGrantValue() != null) {
            total += getGrantValue();
        }
        if (getRecycledCapitalGrantFundValue() != null) {
            total += getRecycledCapitalGrantFundValue();
        }
        if (getDisposalProceedsFundValue() != null) {
            total += getDisposalProceedsFundValue();
        }

        return new BigDecimal(total);
    }

    @Override
    public BigDecimal getGrantAdjustmentAmount(FundingSourceProvider previousVersion) {
        GrantSourceBlock approvedBlock = (GrantSourceBlock) previousVersion;
        // approved block should never be null
        if (approvedBlock == null || (this.isAssociatedProject() && !approvedBlock.isAssociatedProject())) {
            return BigDecimal.ZERO;
        } else {
            Long newGrant = this.getGrantValue() == null ? 0L : this.getGrantValue();
            Long oldGrant = approvedBlock.getGrantValue() == null ? 0L : approvedBlock.getGrantValue();
            return new BigDecimal(newGrant - oldGrant);
        }
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.GrantSource;
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);

        GrantSourceBlock clone = (GrantSourceBlock) target;
        clone.setGrantValue(this.getGrantValue());
        clone.setDisposalProceedsFundValue(this.getDisposalProceedsFundValue());
        clone.setRecycledCapitalGrantFundValue(this.getRecycledCapitalGrantFundValue());
        clone.setZeroGrantRequested(this.isZeroGrantRequested());
        clone.setAssociatedProject(this.isAssociatedProject());
        clone.setStrategicFunding(this.getStrategicFunding());
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        GrantSourceBlock otherGrantSourceBlock = (GrantSourceBlock) other;
        if (!Objects.equals(zeroGrantRequested, otherGrantSourceBlock.zeroGrantRequested)) {
            differences.add(new ProjectDifference(this, "zeroGrantRequested"));
        }

        if (!Objects.equals(associatedProject, otherGrantSourceBlock.associatedProject)) {
            differences.add(new ProjectDifference(this, "associatedProject"));
        }

        if (!Objects.equals(grantValue, otherGrantSourceBlock.grantValue)) {
            differences.add(new ProjectDifference(this, "grantValue"));
        }

        if (!Objects.equals(recycledCapitalGrantFundValue, otherGrantSourceBlock.recycledCapitalGrantFundValue)) {
            differences.add(new ProjectDifference(this, "recycledCapitalGrantFundValue"));
        }

        if (!Objects.equals(disposalProceedsFundValue, otherGrantSourceBlock.disposalProceedsFundValue)) {
            differences.add(new ProjectDifference(this, "disposalProceedsFundValue"));
        }

        if (!Objects.equals(strategicFunding, otherGrantSourceBlock.strategicFunding)) {
            differences.add(new ProjectDifference(this, "strategicFunding"));
        }

        if (!Objects.equals(this.getTotalGrantRequested(), otherGrantSourceBlock.getTotalGrantRequested())) {
            differences.add(new ProjectDifference(this, "totalGrantRequested"));
        }
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

    @Override
    public Map<GrantType, BigDecimal> getFundingRequested() {
        Map<GrantType, BigDecimal> existingRequests = new HashMap<>();
        existingRequests.put(GrantType.Grant, toBigDecimal(grantValue));
        existingRequests.put(GrantType.RCGF, toBigDecimal(recycledCapitalGrantFundValue));
        existingRequests.put(GrantType.DPF, toBigDecimal(disposalProceedsFundValue));
        return existingRequests;
    }

}
