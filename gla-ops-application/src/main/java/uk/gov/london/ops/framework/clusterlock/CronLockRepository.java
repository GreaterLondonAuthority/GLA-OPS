/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.framework.clusterlock;

import org.springframework.stereotype.Repository;
import uk.gov.london.ops.framework.jpa.ReadOnlyRepository;

import java.util.List;

@Repository
public interface CronLockRepository extends ReadOnlyRepository<CronLock, String> {
    List<CronLock> findAllByOrderByCreatedDateAsc();
}
