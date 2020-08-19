/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

/**
 * Any Service that needs notification for block enrichment implement this interface and it will be picked
 * up by project service, and called once the enrichment  is required
 * Created by chris on 10/07/2017.
 */
public interface EnrichmentRequiredListener {

    /**
     * Implement to be informed a project requires enrichment.
     * @param project - project should be checked ot ensure relevant block is present
     * @param enrichmentForComparison - is this enrichment on for CME report.
     */
    void enrichProject(Project project, boolean enrichmentForComparison);

}
