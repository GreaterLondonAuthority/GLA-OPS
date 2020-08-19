/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.di;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;

import javax.transaction.Transactional;


@Transactional
@Component
public class ProductionOrganisationInitialiser implements DataInitialiserModule {

    public static final String SGW_SYSTEM_USER = "some@email.com";

    @Override
    public String getName() {
        return "Production organisation initialiser";
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void addReferenceData() {}

    @Override
    public void addUsers() {
    }

    @Override
    public void addOrganisations() {
    }

    @Override
    public void addTemplates() {}

    @Override
    public void addProgrammes() {}

    @Override
    public void addProjects() {}

    @Override
    public void addSupplementalData() {}

    @Override
    public void afterInitialisation() {
    }

    @Override
    public int executionOrder() {
        return 1;
    }

}
