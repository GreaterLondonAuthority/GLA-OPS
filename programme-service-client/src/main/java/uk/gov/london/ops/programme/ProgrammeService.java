/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProgrammeService {

    List<ProgrammeDetailsSummary> getProgrammeDetailsSummaries();

    List<ProgrammeDetailsSummary> getProgrammeDetailsSummariesByTemplate(Integer templateId);

    ProgrammeDetailsSummary getProgrammeDetailsSummary(Integer programmeId);

    ProgrammeDetailsSummary getProgrammeDetailsSummary(String programmeName);

    Set<Integer> getAssessmentTemplatesForCurrentUser(Integer programmeId, Integer templateId);

    Integer countByAssessmentTemplateId(Integer assessmentTemplateId);

    void grantOrganisationAccess(Integer programmeId, Integer templateId, Integer organisationId);

    Map<String, String> getTemplateIdsAndNamesForProgrammes(Integer[] programmeIds);

}
