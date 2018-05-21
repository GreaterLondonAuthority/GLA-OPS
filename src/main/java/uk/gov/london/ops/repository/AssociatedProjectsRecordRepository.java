/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import uk.gov.london.ops.domain.ProgrammeOrganisationID;
import uk.gov.london.ops.domain.project.AssociatedProjectsRecord;

/**
 * Created by chris on 02/02/2017.
 */
public interface AssociatedProjectsRecordRepository extends ReadOnlyRepository<AssociatedProjectsRecord, ProgrammeOrganisationID> {


}
