/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.receipt;

import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.WbsCodeEntity;
import uk.gov.london.ops.project.block.*;

import javax.persistence.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity(name = "receipts_block")
@DiscriminatorValue("RECEIPTS")
@JoinData(sourceTable = "receipts_block", sourceColumn = "id", targetTable = "project_block",
        targetColumn = "id", joinType = Join.JoinType.OneToOne,
        comment = "the receipts block is a subclass of the project block and shares a common key")
public class ReceiptsBlock extends BaseFinanceBlock {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = WbsCodeEntity.class)
    @JoinColumn(name = "block_id")
    private Set<WbsCodeEntity> wbsCodes = new HashSet<>();

    @Transient
    // TODO Remove single summary
    private AnnualReceiptsSummary annualReceiptsSummary;

    @Transient
    private List<AnnualReceiptsSummary> annualReceiptsSummaries = new ArrayList<>();

    public ReceiptsBlock() {
    }

    public ReceiptsBlock(Project project) {
        super(project);
    }

    public Set<WbsCodeEntity> getWbsCodes() {
        return wbsCodes;
    }

    public void setWbsCodes(Set<WbsCodeEntity> wbsCodes) {
        this.wbsCodes = wbsCodes;
    }

    public AnnualReceiptsSummary getAnnualReceiptsSummary() {
        return annualReceiptsSummary;
    }

    public void setAnnualReceiptsSummary(AnnualReceiptsSummary annualReceiptsSummary) {
        this.annualReceiptsSummary = annualReceiptsSummary;
    }

    @Override
    public ProjectBlockType getBlockType() {
        return ProjectBlockType.Receipts;
    }

    @Override
    public boolean isComplete() {
        return isVisited();
    }

    public void merge(ReceiptsBlock updatedBlock) {
        this.wbsCodes.clear();
        this.wbsCodes.addAll(updatedBlock.wbsCodes);
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);

        if (ProjectBlockType.Receipts.equals(target.getBlockType())) {
            ReceiptsBlock clone = (ReceiptsBlock) target;
            for (WbsCodeEntity wbsCode : wbsCodes) {
                clone.getWbsCodes().add(new WbsCodeEntity(wbsCode.getCode(), wbsCode.getType()));
            }
        }
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock otherBlock, ProjectDifferences differences) {
        super.compareBlockSpecificContent(otherBlock, differences);

        ReceiptsBlock other = (ReceiptsBlock) otherBlock;

        Map<String, WbsCodeEntity> thisWBSCodes = this.getWbsCodes().stream()
                .collect(Collectors.toMap(WbsCodeEntity::getComparisonId, Function.identity()));

        Map<String, WbsCodeEntity> otherWBSCodes = other.getWbsCodes().stream()
                .collect(Collectors.toMap(WbsCodeEntity::getComparisonId, Function.identity()));

        // remove matches in both as wbs codes cannot be edited
        thisWBSCodes.keySet().removeIf(code -> otherWBSCodes.remove(code) != null);

        for (WbsCodeEntity wbsCode : thisWBSCodes.values()) {
            differences.add(new ProjectDifference(wbsCode, ProjectDifference.DifferenceType.Addition));
        }

        for (WbsCodeEntity wbsCode : otherWBSCodes.values()) {
            differences.add(new ProjectDifference(wbsCode, ProjectDifference.DifferenceType.Deletion));
        }

        Map<String, AnnualReceiptsSummary> thisReceipts = this.getAnnualReceiptsSummaries().stream()
                .collect(Collectors.toMap(AnnualReceiptsSummary::getComparisonId, Function.identity()));

        Map<String, AnnualReceiptsSummary> otherReceipts = other.getAnnualReceiptsSummaries().stream()
                .collect(Collectors.toMap(AnnualReceiptsSummary::getComparisonId, Function.identity()));

        for (Iterator<String> iterator = thisReceipts.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            AnnualReceiptsSummary thisSummary = thisReceipts.get(key);
            AnnualReceiptsSummary otherSummary = otherReceipts.get(key);

            if (otherSummary != null) {
                if (GlaUtils.compareBigDecimals(thisSummary.getTotalForPastMonths().getActual(),
                        otherSummary.getTotalForPastMonths().getActual()) != 0) {
                    differences.add(new ProjectDifference(thisSummary, "actual"));
                }
                if (GlaUtils.compareBigDecimals(thisSummary.getTotalForCurrentAndFutureMonths().getForecast(),
                        otherSummary.getTotalForCurrentAndFutureMonths().getForecast()) != 0) {
                    differences.add(new ProjectDifference(thisSummary, "forecast"));
                }
                iterator.remove();
                otherReceipts.remove(key);
            }
        }

        for (AnnualReceiptsSummary receiptsSummary : thisReceipts.values()) {
            differences.add(new ProjectDifference(receiptsSummary, ProjectDifference.DifferenceType.Addition));
        }
        for (AnnualReceiptsSummary receiptsSummary : otherReceipts.values()) {
            differences.add(new ProjectDifference(receiptsSummary, ProjectDifference.DifferenceType.Deletion));
        }
    }

    public List<AnnualReceiptsSummary> getAnnualReceiptsSummaries() {
        return annualReceiptsSummaries;
    }

    public void setAnnualReceiptsSummaries(List<AnnualReceiptsSummary> annualReceiptsSummaries) {
        this.annualReceiptsSummaries = annualReceiptsSummaries;
    }

    @Override
    public boolean isSelfContained() {
        return false;
    }


    @Override
    protected boolean canShowYear(Integer year) {
        return true;
    }
}
