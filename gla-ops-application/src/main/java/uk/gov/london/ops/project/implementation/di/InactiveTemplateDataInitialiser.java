/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.programme.ProgrammeBuilder;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.Template;

import javax.transaction.Transactional;

@Transactional
@Component
public class InactiveTemplateDataInitialiser implements DataInitialiserModule {

    @Autowired
    private ProgrammeBuilder programmeBuilder;

    @Autowired
    private TemplateServiceImpl templateService;

    private Template inactiveTemplate;

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void addTemplates() {
        inactiveTemplate = templateService.findByName("");
    }

    @Override
    public void addProgrammes() {
        Programme inactiveProgramme = programmeBuilder.createTestProgramme("", false, true,
                inactiveTemplate);
        inactiveProgramme.getTemplatesByProgramme().iterator().next().setStatus(ProgrammeTemplate.Status.Inactive);
    }

}
