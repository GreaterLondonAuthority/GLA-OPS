/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.audit.implementation.AuditRepository;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.service.UserService;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    public void auditCurrentUserActivity(String summary,  ActivityType type, String userAffected, Integer organisationId, BigDecimal amount) {
        String username = userService.currentUser().getUsername();
        auditActivityForUser(username, summary, null, null, type, userAffected, organisationId, amount);

    }

    public void auditActivityForUser(String userName, String summary) {
        auditActivityForUser(userName, summary, null, null, null);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void auditActivityForUserNewTransaction(String userName, String summary) {
        auditActivityForUser(userName, summary);
    }

    private String buildSummary(EntityType entityType, Integer entityId, ActivityType type, String username) {
        return String.format("%s %d %s by %s", entityType, entityId, type.getVerb(), username);
    }

    private void auditActivityForUser(String userName, String summary, EntityType entityType, Integer entityId, ActivityType type) {
        auditActivityForUser(userName, summary, entityType, entityId, type, null, null, null);
    }
    private void auditActivityForUser(String userName, String summary, EntityType entityType, Integer entityId, ActivityType type, String userAffected, Integer organisationId, BigDecimal amount) {
        AuditableActivity activity = new AuditableActivity();
        activity.setSummary(StringUtils.substring(summary,0, 255));
        activity.setUserName(userName);
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);
        activity.setType(type);
        activity.setAffectedUserName(userAffected);
        activity.setOrganisationId(organisationId);
        activity.setAmount(amount);
        activity.setTimestamp(environment.now());

        repository.save(activity);
    }

    public List findMostRecent() {
        return repository.findMostRecent();
    }

    public Page<AuditableActivity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<AuditableActivity> findAll(String username, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return repository.findAll(username,
                GlaUtils.toOffsetDateTime(fromDate),
                GlaUtils.toOffsetDateTime(toDate != null? toDate.plusDays(1) : null),
                pageable);
    }

    public List<AuditableActivity> findAll() {
        return repository.findAll();
    }

    public List<AuditableActivity> findAllBySummaryContainingIgnoreCase(String summary) {
        return repository.findAllBySummaryContainingIgnoreCase(summary);
    }

}
