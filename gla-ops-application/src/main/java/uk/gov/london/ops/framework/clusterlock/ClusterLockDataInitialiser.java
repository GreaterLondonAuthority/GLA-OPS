/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.framework.clusterlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;

import javax.transaction.Transactional;

@Transactional
@Component
public class ClusterLockDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ClusterLockService clusterLockService;

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void beforeInitialisation() {
        log.info("Initialising cluster locks");
    }

    @Override
    public void addReferenceData() {

    }

    @Override
    public void addUsers() {

    }

    @Override
    public void addOrganisations() {

    }

    @Override
    public void addTemplates() {

    }

    @Override
    public void addProgrammes() {

    }

    @Override
    public void addProjects() {

    }

    @Override
    public void addSupplementalData() {
        clusterLockService.save(new ClusterLock(ClusterLock.Type.EMAIL, null, null));
        clusterLockService.save(new ClusterLock(ClusterLock.Type.SCHEDULED_NOTIFICATION, null, null));
    }

    @Override
    public void afterInitialisation() {

    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }
}
