/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.ActivityType;
import uk.gov.london.ops.domain.AuditableActivity;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.repository.AuditRepository;

/**
 * Service for recording auditable activity.
 *
 * @author Steve Leach
 */
@Service
public class AuditService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    AuditRepository repository;

    @Autowired
    UserService userService;

    @Autowired
    Environment environment;

    /**
     * Record an auditable activity by the current user.
     */
    public void auditCurrentUserActivity(String summary) {
        auditActivityForUser(userService.currentUser().getUsername(), summary);
    }

    public void auditCurrentUserActivity(EntityType entityType, Integer entityId, ActivityType type) {
        String username = userService.currentUser().getUsername();
        String summary = buildSummary(entityType, entityId, type, username);
        auditActivityForUser(username, summary, entityType, entityId, type);
    }

    public void auditActivityForUser(String userName, String summary) {
        auditActivityForUser(userName, summary, null, null, null);
    }

    private String buildSummary(EntityType entityType, Integer entityId, ActivityType type, String username) {
        return String.format("%s %d %s by %s", entityType, entityId, type.getVerb(), username);
    }

    private void auditActivityForUser(String userName, String summary, EntityType entityType, Integer entityId, ActivityType type) {
        AuditableActivity activity = new AuditableActivity();
        activity.setSummary(StringUtils.substring(summary,0, 255));
        activity.setUserName(userName);
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);
        activity.setType(type);
        activity.setTimestamp(environment.now());

        repository.save(activity);
    }

}
