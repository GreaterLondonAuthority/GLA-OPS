/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.project.*;
import uk.gov.london.ops.payment.FinanceService;
import uk.gov.london.ops.payment.LedgerType;
import uk.gov.london.ops.payment.ProjectLedgerEntry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chris on 20/09/2017.
 */
@Component
public class MilestoneReportProfile extends BaseReportProfile {

    private static final String SUPPLEMENTARY_CATEGORY = "Supplementary";
    private static final String MILESTONE_CATEGORY = "Milestone";
    private Set<String> headers;

    @Autowired
    private FinanceService financeService;

    @Resource(name = "milestoneSummaryReport")
    private Map<String, String> headerMapper;



    /**
     * This set up the metadata for Borough report: Header names
     * (headers) and field metadata
     * (mapper)
     *
     * Notes:
     * - headers contains a sorted list of header names. I is used
     * to generate the CSV file. This just order the map headerMapper,
     * which provides the header name for every column.
     *
     * - mapper contains a map with the fields of the
     * BoroughReportItem entity, indexed by header name. This will helps to
     * retrieves the field's value dynamically based on the header. This is done
     * by using headerMapper, which is indexed by column name in the
     * database and the
     *
     * @column annotation in the BoroughReportItem.
     */
    @PostConstruct
     void boroughReportMetadataSetUp() {
        //LinkedHashSet is used to keep the inserting order. DON'T change it
        headers = new LinkedHashSet<>();
        headers.add(headerMapper.get("programme_name"));
        headers.add(headerMapper.get("org_name"));
        headers.add(headerMapper.get("org_id"));
        headers.add(headerMapper.get("project_id"));
        headers.add(headerMapper.get("project_status"));
        headers.add(headerMapper.get("start_site_auth_date"));
        headers.add(headerMapper.get("start_site_appr_date"));
        headers.add(headerMapper.get("rcgf_at_sos"));
        headers.add(headerMapper.get("dpf_at_sos"));
        headers.add(headerMapper.get("grant_at_sos"));
        headers.add(headerMapper.get("supplemental_pay_date"));
        headers.add(headerMapper.get("total_units_LAR"));
        headers.add(headerMapper.get("total_units_LLR"));
        headers.add(headerMapper.get("total_units_LSO"));
        headers.add(headerMapper.get("total_units_OA"));


    }

    void populateProjectDetailsForReport(Project project,  Map<String, Object> map) {

        map.put(headerMapper.get("programme_name"), getReportFieldValue(project.getProgramme().getName()));
        map.put(headerMapper.get("org_name"), getReportFieldValue(project.getOrganisation().getName()));
        map.put(headerMapper.get("org_id"), getReportFieldValue(project.getOrganisation().getId()));
        map.put(headerMapper.get("project_id"), getReportFieldValue(project.getId()));
        map.put(headerMapper.get("project_status"), getReportFieldValue(project.getStatusName() +
                (project.getSubStatusName() == null ? "" : " " + project.getSubStatusName())));

    }

    void populateApprovedMilestone(Map<String, Object> map, Project project, Integer extId) {
        List<NamedProjectBlock> blocksByType = project.getBlocksByType(ProjectBlockType.Milestones).stream()
                .filter(m1 -> m1.getBlockStatus() != NamedProjectBlock.BlockStatus.UNAPPROVED)
                .sorted(Comparator.comparing(NamedProjectBlock::getVersionNumber))
                .collect(Collectors.toList());


        for (NamedProjectBlock namedProjectBlock : blocksByType) {
            ProjectMilestonesBlock block = (ProjectMilestonesBlock) namedProjectBlock;
            Optional<Milestone> first = block.getMilestones().stream().filter(m1 -> extId.equals(m1.getExternalId())).findFirst();
            if (first.isPresent()) {
                Milestone m = first.get();
                if (ClaimStatus.Approved.equals(m.getClaimStatus())) {
                    // first approved
                    map.put(headerMapper.get("start_site_auth_date"), getReportFieldValue(block.getApprovalTime()));

                    extractPaymentDetails(map, project, extId);
                    extractSupplementalPaymentDetails(map, project, extId);
                }
            }
        }
    }

    void extractUnitDetails(Map<String, Object> map, Project project) {

        List<NamedProjectBlock> blocksByType = project.getBlocksByType(ProjectBlockType.UnitDetails).stream()
                .filter(m1 -> m1.getBlockStatus() != NamedProjectBlock.BlockStatus.UNAPPROVED)
                .sorted(Comparator.comparing(NamedProjectBlock::getVersionNumber))
                .collect(Collectors.toList());


        if (blocksByType != null && blocksByType.size() > 0) {
            UnitDetailsBlock units = (UnitDetailsBlock) blocksByType.get(blocksByType.size() - 1);
            for (UnitDetailsBlock.TenureProfile tenureProfile : units.getTenureProfiles().getBreakdown()) {
                switch (tenureProfile.getExtId()) {
                    case 4000:
                        map.put(headerMapper.get("total_units_LAR"), getReportFieldValue(tenureProfile.getTotalUnits()));
                        break;
                    case 4001:
                        map.put(headerMapper.get("total_units_LLR"), getReportFieldValue(tenureProfile.getTotalUnits()));
                        break;
                    case 4002:
                        map.put(headerMapper.get("total_units_LSO"), getReportFieldValue(tenureProfile.getTotalUnits()));
                        break;
                    case 4003:
                        map.put(headerMapper.get("total_units_OA"), getReportFieldValue(tenureProfile.getTotalUnits()));
                        break;
                }
            }
        }
    }

    void extractSupplementalPaymentDetails(Map<String, Object> map, Project project, Integer extId) {
        List<ProjectLedgerEntry> supplementals = financeService.findAllByProjectIdAndCategoryAndExternalId(project.getId(), SUPPLEMENTARY_CATEGORY, extId);
        Optional<ProjectLedgerEntry> first = supplementals.stream().filter(p1 -> p1.getAuthorisedOn() != null).sorted((p1, p2) -> p2.getAuthorisedOn().compareTo(p1.getAuthorisedOn())).findFirst();
        if (first.isPresent()) {
            ProjectLedgerEntry projectLedgerEntry = first.get();
            map.put(headerMapper.get("supplemental_pay_date"), getReportFieldValue(projectLedgerEntry.getAuthorisedOn()));
        }

    }

    void extractPaymentDetails(Map<String, Object> map, Project project, Integer extId) {
        List<ProjectLedgerEntry> allByProjectId = financeService.findAllByProjectIdAndCategoryAndExternalId(project.getId(), MILESTONE_CATEGORY, extId);

        for (ProjectLedgerEntry projectLedgerEntry : allByProjectId) {
            if (projectLedgerEntry.getAuthorisedBy() != null) {
                map.put(headerMapper.get("start_site_appr_date"), getReportFieldValue(projectLedgerEntry.getAuthorisedOn()));
                if (LedgerType.RCGF.equals(projectLedgerEntry.getLedgerType())) {
                    map.put(headerMapper.get("rcgf_at_sos"), getReportFieldValue(projectLedgerEntry.getValue()));
                } else if (LedgerType.DPF.equals(projectLedgerEntry.getLedgerType())) {
                    map.put(headerMapper.get("dpf_at_sos"), getReportFieldValue(projectLedgerEntry.getValue()));
                } else if (LedgerType.PAYMENT.equals(projectLedgerEntry.getLedgerType())) {
                    map.put(headerMapper.get("grant_at_sos"), getReportFieldValue(projectLedgerEntry.getValue()));
                }
            }
        }

    }


    /**
     * Gets a list of Projects  and convert them to Map .
     *
     * @param list List of Projects to be converted to Map objects
     * @param milestoneExternalId externalID of the milestones to be recognised
     * @return Map with the field, indexed by header name
     */
    public List<Map<String, Object>> convertProjectsToMap(
            final List<Project> list, int milestoneExternalId) {
        final List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {

            for(Project project : list) {
                final Map<String, Object> line = new HashMap<>();

                populateProjectDetailsForReport(project, line);
                populateApprovedMilestone(line, project, milestoneExternalId);
                extractUnitDetails(line, project);
                result.add(line);
            }
        }
        return result;
    }



    public Set<String> getHeaders() {
        return headers;
    }

}
