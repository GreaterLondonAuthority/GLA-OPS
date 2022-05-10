/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.programme.ProgrammeBuilder;
import uk.gov.london.ops.programme.ProgrammeServiceImpl;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.template.TemplateServiceImpl;
import uk.gov.london.ops.project.template.domain.Template;

import javax.transaction.Transactional;

@Transactional
@Component
public class ProgrammeDataInitialiser implements DataInitialiserModule {

    @Autowired
    private ProgrammeServiceImpl programmeService;

    @Autowired
    private TemplateServiceImpl templateService;

    @Autowired
    private ProgrammeBuilder programmeBuilder;

    private Template housingTemplate;
    private Template smallProjects;

    @Override
    public String getName() {
        return "Programme data initialiser";
    }

    @Override
    public int executionOrder() {
        return 49;
    }

    @Override
    public void addTemplates() {
        housingTemplate = templateService.findByName("Approved Provider Route");
        smallProjects = templateService.findByName("Small Projects and Equipment Fund");
    }

    @Override
    public void addProgrammes() {
        Programme disabledPaymentProgramme = programmeBuilder.createTestProgramme("Disabled Payments Programme",
                false, true, housingTemplate, smallProjects);

        for (ProgrammeTemplate programmeTemplate : disabledPaymentProgramme.getTemplatesByProgramme()) {
            programmeTemplate.setPaymentsEnabled(false);
        }
        programmeService.save(disabledPaymentProgramme);

        Programme noWBSCode = programmeBuilder.createTestProgramme("Programme without WBS", false, true, housingTemplate);
        noWBSCode.setWbsCodeForTemplate(housingTemplate.getId(), ProgrammeTemplate.WbsCodeType.Capital, null);
        for (ProgrammeTemplate programmeTemplate : noWBSCode.getTemplatesByProgramme()) {
            if (housingTemplate.getId().equals(programmeTemplate.getTemplate().getId())) {
                programmeTemplate.setDefaultWbsCodeType(ProgrammeTemplate.WbsCodeType.Capital);
            }
        }
        programmeService.save(noWBSCode);

        Programme noCECode = programmeBuilder.createTestProgramme("Programme without CE", false, true, housingTemplate);
        noCECode.setCeCodeForTemplate(housingTemplate.getId(), null);
        for (ProgrammeTemplate programmeTemplate : noCECode.getTemplatesByProgramme()) {
            if (housingTemplate.getId().equals(programmeTemplate.getTemplate().getId())) {
                programmeTemplate.setDefaultWbsCodeType(ProgrammeTemplate.WbsCodeType.Capital);
            }
        }
        programmeService.save(noCECode);
    }

}
