/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.di.DataInitialiserModule;

@Component
public class FileDataInitialiser implements DataInitialiserModule {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public String getName() {
        return "File data initialiser";
    }

    @Override
    public void beforeInitialisation() {}

    @Override
    public void cleanupOldData() {
        jdbcTemplate.update("delete from file");
    }

    @Override
    public void addReferenceData() {}

    @Override
    public void addUsers() {}

    @Override
    public void addOrganisations() {}

    @Override
    public void addTemplates() {}

    @Override
    public void addProgrammes() {}

    @Override
    public void addProjects() {}

    @Override
    public void addSupplementalData() {}

    @Override
    public void afterInitialisation() {}

}
