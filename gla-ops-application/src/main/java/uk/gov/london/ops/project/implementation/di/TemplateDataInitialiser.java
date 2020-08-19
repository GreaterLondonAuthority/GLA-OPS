/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;

import javax.transaction.Transactional;

/**
 * @deprecated - use a feature-aligned DataInitialiser module instead
 */
@Transactional
@Component
public class TemplateDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());

    public static final int DEFAULT_CONFIG_GROUP_ID = 1;

    @Override
    public String getName() {
        return "Template data initialiser";
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void addReferenceData() {
    }

    @Override
    public void addUsers() {}

    @Override
    public void addOrganisations() {}

    @Override
    public void addTemplates() {
    }

    @Override
    public void addProgrammes() {
    }

    @Override
    public void addProjects() {}

    @Override
    public void addSupplementalData() {}

    @Override
    public void afterInitialisation() {
    }

    @Override
    public int executionOrder() {
        return 11;
    }
}
