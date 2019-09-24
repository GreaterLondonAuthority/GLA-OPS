/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.domain.DatabaseUpdate;
import uk.gov.london.ops.repository.DatabaseUpdateRepository;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.london.ops.domain.DatabaseUpdate.Status.*;


/**
 * Logic for approving, running and requesting updates to the database.
 *
 * @author Rob Bettison
 */
@Service
@Component
public class DatabaseUpdateService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    DatabaseUpdateRepository databaseUpdateRepository;

    @Autowired
    Environment environment;

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    FeatureStatus featureStatus;

    void checkSqlEditorEnabled() throws ForbiddenAccessException {
        if(!featureStatus.isEnabled(Feature.SqlEditor)) {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    /**
     * Returns the database update matching the specified ID.
     */
    public DatabaseUpdate getDatabaseUpdate(Integer id) throws ForbiddenAccessException {
        checkSqlEditorEnabled();
        return databaseUpdateRepository.findById(id).orElse(null);
    }

    /**
     * Creates a new database update entity.
     */
    public DatabaseUpdate createDatabaseUpdate(DatabaseUpdate update) throws ForbiddenAccessException  {
        checkSqlEditorEnabled();

        String currentUserName = userService.currentUsername();

        DatabaseUpdate databaseUpdate = new DatabaseUpdate(update.getSql(), currentUserName, environment.now(), false,update.getSummary(),update.getTrackingId());
        databaseUpdateRepository.save(databaseUpdate);
        log.debug("Update awaiting approval: " + update);
        return databaseUpdate;
    }

    /**
     * Saves changes to an existing database update entity.
     */
    public DatabaseUpdate saveDatabaseUpdate(DatabaseUpdate newVersion, Integer id) throws ForbiddenAccessException, ValidationException{
        checkSqlEditorEnabled();

        validateUpdate(id,newVersion);

        databaseUpdateRepository.save(newVersion);

        if (newVersion.hasStatus(Approved)) {
            executeDatabaseUpdate(newVersion);
        }

        return newVersion;
    }

    void validateUpdate(Integer id, DatabaseUpdate newVersion) throws ValidationException {
        String currentUserName = userService.currentUsername();

        DatabaseUpdate oldVersion = databaseUpdateRepository.findById(id).orElse(null);

        if (oldVersion == null) {
            throw new ValidationException("Cannot find database update with ID " + id);
        }

        if (!oldVersion.getId().equals(newVersion.getId())) {
            throw new ValidationException("ID incorrect.");
        }

        if (isBeingApproved(newVersion,oldVersion)) {
            if (!currentUserCanApprove(currentUserName, oldVersion)) {
                throw new ValidationException("User cannot action this update.");
            }

            if (!oldVersion.hasStatus(AwaitingApproval)) {
                throw new ValidationException("Update cannot be approved if not awaiting approval.");
            }

            if (!oldVersion.getSql().equals(newVersion.getSql())) {
                throw new ValidationException("SQL has changed.");
            }

            if (!newVersion.isPpd()) {
                throw new ValidationException("Cannot approve if not run in PPD.");
            }

            newVersion.setApprovedOn(environment.now());
            newVersion.setApprovedBy(currentUserName);
        }

        // Don't allow created on/by to be changed
        newVersion.setCreatedBy(oldVersion.getCreatedBy());
        newVersion.setCreatedOn(oldVersion.getCreatedOn());
    }

    boolean isBeingApproved(DatabaseUpdate newVersion, DatabaseUpdate oldVersion) {
        return newVersion.hasStatus(Approved) && !oldVersion.hasStatus(Approved);
    }

    /**
     * Executes an approved database update.
     *
     * The status of the update is set to either Complete or Failed, and if Complete the
     * number of rows affected is recorded.
     */
    public void executeDatabaseUpdate(DatabaseUpdate databaseUpdate) throws ValidationException {
        if (!databaseUpdate.hasStatus(Approved)) {
            throw new ValidationException("Cannot executeDatabaseUpdate an update that is not in approved state");
        }

        try {
            int rowsAffected = jdbcTemplate.update(databaseUpdate.getSql());
            log.info("SQL Update actioned: {}", databaseUpdate.getId());
            databaseUpdate.setStatus(Complete);
            databaseUpdate.setRowsAffected(rowsAffected);
        } catch (DataAccessException e) {
            log.error("SQL Update failed: " + databaseUpdate.getId(), e);
            databaseUpdate.setStatus(Failed);
        } finally {
            databaseUpdateRepository.save(databaseUpdate);
        }
    }

    boolean currentUserCanApprove(String currentUserName, DatabaseUpdate update) {
        String createdBy = update.getCreatedBy();
        if((createdBy != null) && createdBy.equals(currentUserName)) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Finds all existing database update entities.
     */
    public List<DatabaseUpdate> findAll() throws ForbiddenAccessException {
        checkSqlEditorEnabled();
        return databaseUpdateRepository.findAll().stream().sorted(Comparator.comparing(DatabaseUpdate::getCreatedOn, Comparator.naturalOrder())).collect(Collectors.toList());
    }

}
