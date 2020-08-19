/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import static uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConstants.ReportPrefix;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.springframework.util.StringUtils;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.implementation.spe.SimpleProjectExportConfig;

/**
 * Created by chris on 10/11/2016.
 */
@Entity(name = "design_standards")
@DiscriminatorValue("DESIGN")
@JoinData(sourceTable = "design_standards", sourceColumn = "id", targetTable = "project_block", targetColumn = "id",
        joinType = Join.JoinType.OneToOne,
        comment = "the design standards block is a subclass of the project block and shares a common key")
public class DesignStandardsBlock extends NamedProjectBlock {

    public static final String MEETING_DESIGN_GUIDE_PARAM = ReportPrefix.ds_.name() + "meeting_design_guide";
    public static final String REASON_NOT_MEETING_DESIGN_GUIDE_PARAM =
            ReportPrefix.ds_.name() + "reason_not_meeting_design_guide";

    @Column(name = "meeting_design_guide")
    private Boolean meetingLondonHousingDesignGuide;

    @Column(name = "reason_not_meeting_design_guide")
    private String reasonForNotMeetingDesignGuide;

    public DesignStandardsBlock() {
        setBlockType(ProjectBlockType.DesignStandards);
    }

    public Boolean getMeetingLondonHousingDesignGuide() {
        return meetingLondonHousingDesignGuide;
    }

    public void setMeetingLondonHousingDesignGuide(Boolean meetingLondonHousingDesignGuide) {
        this.meetingLondonHousingDesignGuide = meetingLondonHousingDesignGuide;
    }

    public String getReasonForNotMeetingDesignGuide() {
        return reasonForNotMeetingDesignGuide;
    }

    public void setReasonForNotMeetingDesignGuide(String reasonForNotMeetingDesignGuide) {
        this.reasonForNotMeetingDesignGuide = reasonForNotMeetingDesignGuide;
    }

    @Override
    public void merge(NamedProjectBlock namedProjectBlock) {
        DesignStandardsBlock designStandardsBlock = (DesignStandardsBlock) namedProjectBlock;
        this.meetingLondonHousingDesignGuide = designStandardsBlock.getMeetingLondonHousingDesignGuide();
        this.reasonForNotMeetingDesignGuide = Boolean.TRUE.equals(this.meetingLondonHousingDesignGuide) ? null
                : designStandardsBlock.getReasonForNotMeetingDesignGuide();
    }

    @Override
    @Transient
    protected void generateValidationFailures() {

        if (this.getMeetingLondonHousingDesignGuide() == null) {
            this.addErrorMessage("Block1", "meetingLondonHousingDesignGuide", "Specify if meeting London Housing Design Guide");

        } else if (!this.getMeetingLondonHousingDesignGuide()) {
            // reason is mandatory if they do not meet the design standards
            if (this.getReasonForNotMeetingDesignGuide() == null
                    || this.getReasonForNotMeetingDesignGuide().length() == 0
                    || this.getReasonForNotMeetingDesignGuide().length() > 1000) {
                this.addErrorMessage("Block1", "reasonForNotMeetingDesignGuide",
                        "The reason for not meeting Design Guide is mandatory");

            }
        }
    }

    @Transient
    @Override
    public boolean isComplete() {
        return isVisited() && getValidationFailures().size() == 0;
    }

    public Map<String, Object> simpleDataExtract(SimpleProjectExportConfig simpleProjectExportConfig) {
        HashMap<String, Object> data = new HashMap<>();
        if (this.getMeetingLondonHousingDesignGuide() != null) {
            data.put(MEETING_DESIGN_GUIDE_PARAM,
                    this.getMeetingLondonHousingDesignGuide() ? "YES" : "NO");
            if (!this.getMeetingLondonHousingDesignGuide()) {
                data.put(REASON_NOT_MEETING_DESIGN_GUIDE_PARAM,
                        this.getReasonForNotMeetingDesignGuide());
            }
        }
        return data;
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        DesignStandardsBlock designStandardsBlock = (DesignStandardsBlock) target;
        designStandardsBlock.setMeetingLondonHousingDesignGuide(this.getMeetingLondonHousingDesignGuide());
        designStandardsBlock.setReasonForNotMeetingDesignGuide(this.getReasonForNotMeetingDesignGuide());
    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
        DesignStandardsBlock otherDesignStandardsBlock = (DesignStandardsBlock) other;

        if (!Objects.equals(meetingLondonHousingDesignGuide, otherDesignStandardsBlock.meetingLondonHousingDesignGuide)) {
            differences.add(new ProjectDifference(this, "meetingLondonHousingDesignGuide"));
        }

        if (!Objects.equals(StringUtils.trimAllWhitespace(reasonForNotMeetingDesignGuide),
                StringUtils.trimAllWhitespace(otherDesignStandardsBlock.reasonForNotMeetingDesignGuide))) {
            differences.add(new ProjectDifference(this, "reasonForNotMeetingDesignGuide"));
        }
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

}
