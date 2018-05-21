/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import uk.gov.london.ops.domain.report.BoroughReportItem;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An repository to retrieve Borough reports.
 * @see BoroughReportItem
 * @author  Antonio Perez Dieppa
 */
public interface BoroughReportRepository extends ReadOnlyRepository<BoroughReportItem,Integer> {

    List<BoroughReportItem> findByProgrammeIdOrderByBoroughAsc(Integer programmeId);

    /**
     * Default implementation to filter by programmeId and additionally, if present,
     * by project type, project status and borough.
     *
     * Note: Due to the project type, status and borough are optional, we need some
     * additional mechanism to perform this query as the default JPA repository doesn't
     * provide enough flexibility. For future development it may be worthy to look at
     * JPA criteria or QueryDsl, but for the time being this has been implementing by
     * retrieving the data by programmes and do the additional filter in memory. This
     * assumes that there won't be many projects by programme and not too many request
     * concurrently.
     *
     * @param programmeId mandatory Programme
     * @param projectType optional project type
     * @param projectStatus optional project status
     * @param borough optional borough
     *
     * @return A list with the BoroughReport items filtered byt the given params and
     * sorted by borough
     */
    default List<BoroughReportItem> find(final Integer programmeId,
                                         final Integer projectType,
                                         final String projectStatus,
                                         final String borough){


        Function<BoroughReportItem, Boolean> filter = bi ->
                (projectType == null || projectType.equals(bi.getTemplateId()))
                && (borough == null || "".equals(borough) || borough.equals(bi.getBorough()))
                && (projectStatus == null || "".equals(projectStatus) || projectStatus.equals(bi.getStatus()));
        final Collection<BoroughReportItem> projectReports = this.findByProgrammeIdOrderByBoroughAsc(programmeId);
        return projectReports
                .stream()
                .filter(filter::apply)
                .collect(Collectors.toList());
    }
}