/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.london.common.GlaUtils;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.audit.implementation.AuditRepository;
import uk.gov.london.ops.framework.EntityType;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.london.ops.framework.OPSUtils.currentUsername;

/**
 * Service for recording auditable activity.
 *
 * @author Steve Leach
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final int MAX_SUMMARY_SIZE = 4000;

    final AuditRepository repository;
    final Environment environment;

    public AuditServiceImpl(AuditRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }

    /**
     * Record an auditable activity by the current user.
     */
    public void auditCurrentUserActivity(String summary) {
        auditActivityForUser(currentUsername(), summary);
    }

    public void auditCurrentUserActivity(EntityType entityType, Integer entityId, ActivityType type) {
        String username = currentUsername();
        String summary = buildSummary(entityType, entityId, type, username);
        auditActivityForUser(username, summary, entityType, entityId, type);
    }

    public void auditCurrentUserActivity(String summary, ActivityType type, String userAffected, Integer organisationId,
                                         BigDecimal amount) {
        String username = currentUsername();
        auditActivityForUser(username, summary, null, null, type, userAffected, organisationId, amount);
    }

    public void auditActivityForUser(String userName, String summary) {
        auditActivityForUser(userName, summary, null, null, null);
    }

    private void auditActivityForUser(String userName, String summary, EntityType entityType, Integer entityId, ActivityType type) {
        auditActivityForUser(userName, summary, entityType, entityId, type, null, null, null);
    }

    private void auditActivityForUser(String userName, String summary, EntityType entityType, Integer entityId, ActivityType type,
                                      String userAffected, Integer organisationId, BigDecimal amount) {
        AuditableActivityEntity activity = new AuditableActivityEntity();
        activity.setSummary(StringUtils.substring(summary, 0, MAX_SUMMARY_SIZE));
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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void auditActivityForUserNewTransaction(String userName, String summary) {
        auditActivityForUser(userName, summary);
    }

    private String buildSummary(EntityType entityType, Integer entityId, ActivityType type, String username) {
        return String.format("%s %d %s by %s", entityType, entityId, type.getVerb(), username);
    }

    public Page<AuditableActivityEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<AuditableActivityEntity> findAll(String username, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return repository.findAll(username,
                GlaUtils.toOffsetDateTime(fromDate),
                GlaUtils.toOffsetDateTime(toDate != null ? toDate.plusDays(1) : null),
                pageable);
    }

    public List<AuditableActivity> findAllBySummaryContainingIgnoreCase(String summary) {
        List<AuditableActivityEntity> entities = repository.findAllBySummaryContainingIgnoreCase(summary);
        return toModels(entities);
    }

    List<AuditableActivity> toModels(List<AuditableActivityEntity> entities) {
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    AuditableActivity toModel(AuditableActivityEntity entity) {
        return new AuditableActivity(
                entity.getId(),
                entity.getUserName(),
                entity.getAffectedUserName(),
                entity.getOrganisationId(),
                entity.getAmount(),
                entity.getTimestamp(),
                entity.getSummary(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getType()
        );
    }

}
