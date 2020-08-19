/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.organisation.OrganisationService;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.template.domain.Template;
import uk.gov.london.ops.user.UserService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.london.ops.organisation.model.Organisation.GLA_HNL_ID;
import static uk.gov.london.ops.payment.ProjectLedgerEntry.DEFAULT_LEDGER_CE_CODE;
import static uk.gov.london.ops.user.UserBuilder.DATA_INITIALISER_USER;

@Component
public class ProgrammeBuilder {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private UserService userService;

    @Autowired
    private Environment environment;

    public Programme createTestProgramme(String name, boolean restricted, boolean enabled, Template... templates) {
        return createTestProgramme(name, restricted, enabled, GLA_HNL_ID, templates);
    }

    public Programme createTestProgramme(String name, boolean restricted, boolean enabled, List<String> supportedReports, Template... templates) {
        return createTestProgramme(name, restricted, enabled, GLA_HNL_ID, supportedReports, templates);
    }

    public Programme createTestProgramme(String name, boolean restricted, boolean enabled, Integer managingOrgId, Template... templates) {
        return createTestProgramme(name, restricted, enabled, managingOrgId, null, templates);
    }

    public Programme addPublicProfileDetails(Programme programme, String description, BigDecimal totalFunding, String website) {
        programme.setDescription(description);
        programme.setTotalFunding(totalFunding);
        programme.setWebsiteLink(website);

        return programmeService.save(programme);
    }

    public Programme createTestProgramme(String name,
                                         boolean restricted,
                                         boolean enabled,
                                         Integer managingOrgId,
                                         List<String> supportedReports,
                                         Template... templates) {
        try {
            Programme programme = new Programme();
            programme.setName(name);
            programme.setCreator(userService.find(DATA_INITIALISER_USER.toLowerCase()));
            programme.setCreatedOn(environment.now());
            programme.setRestricted(restricted);
            programme.setEnabled(enabled);
            programme= programmeService.save(programme);


            programme.setTemplatesByProgramme(toSetWithoutNulls(programme, templates));
            for (ProgrammeTemplate programmeTemplate : programme.getTemplatesByProgramme()) {
                programmeTemplate.setWbsCode(ProgrammeTemplate.WbsCodeType.Capital, programme.getName().toLowerCase().substring(0,3) +"_cap_"+ programmeTemplate.getTemplate().getId());
                programmeTemplate.setWbsCode(ProgrammeTemplate.WbsCodeType.Revenue, programme.getName().toLowerCase().substring(0,3) +"_rev_"+ programmeTemplate.getTemplate().getId());
                programmeTemplate.setDefaultWbsCodeType(ProgrammeTemplate.WbsCodeType.Capital);
                programmeTemplate.setCeCode(DEFAULT_LEDGER_CE_CODE);
                programmeTemplate.getTemplate().setStatus(Template.TemplateStatus.Active);
            }


            if (managingOrgId != -1) {
                programme.setManagingOrganisation(organisationService.findOne(managingOrgId));
            }
            programme.setSupportedReports(supportedReports);
            return programmeService.save(programme);
        } catch (RuntimeException e) {
            log.error("Error creating test programme " + name, e);
            return null;
        }
    }

    public  Set<ProgrammeTemplate> toSetWithoutNulls(Programme programme, Template[] templates) {
        HashSet<ProgrammeTemplate> templateSet = new HashSet<>();
        for (Template t : templates) {
            if (t != null) {
                templateSet.add(new ProgrammeTemplate(programme, t));
            }
        }
        return templateSet;
    }

}
