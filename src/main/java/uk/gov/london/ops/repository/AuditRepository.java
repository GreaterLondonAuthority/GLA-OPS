/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.AuditableActivity;

import java.util.List;

/**
 * Spring Data repository for Audit records.
 *
 * @author Steve Leach
 */
public interface AuditRepository extends JpaRepository<AuditableActivity, Integer> {

    List<AuditableActivity> findAllBySummaryContainingIgnoreCase(String summary);

}
