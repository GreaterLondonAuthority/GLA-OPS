/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.implementation;

import org.springframework.stereotype.Repository;
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammePublicProfileSummary;

import java.util.List;

@Repository
public interface ProgrammePublicProfileSummaryRepository extends ReadOnlyRepository<ProgrammePublicProfileSummary, Integer> {

    List<ProgrammePublicProfileSummary> findAllByStatusAndEnabledAndRestricted(Programme.Status status, boolean enabled,
            boolean restricted);

}
