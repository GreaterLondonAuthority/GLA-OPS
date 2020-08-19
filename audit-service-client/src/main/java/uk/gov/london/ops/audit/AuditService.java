/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit;

import uk.gov.london.ops.framework.EntityType;

import java.math.BigDecimal;
import java.util.List;

public interface AuditService {

    void auditCurrentUserActivity(String summary);

    void auditCurrentUserActivity(EntityType entityType, Integer entityId, ActivityType type);

    void auditCurrentUserActivity(String summary,  ActivityType type, String userAffected, Integer organisationId, BigDecimal amount);

    void auditActivityForUser(String userName, String summary);

    void auditActivityForUserNewTransaction(String userName, String summary);

    List<AuditableActivity> findAllBySummaryContainingIgnoreCase(String summary);

}
