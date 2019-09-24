/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.annualsubmission.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.annualsubmission.*;
import uk.gov.london.ops.di.DataInitialiserModule;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import static uk.gov.london.ops.annualsubmission.AnnualSubmissionStatusType.Actual;
import static uk.gov.london.ops.di.builders.OrganisationBuilder.TEST_ORG_ID_1;

/**
 * Fixes issue with missing DPF assessments
 */
@Transactional
@Component
public class AnnualSubmissionDPFResolutionDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());


    @Autowired
    private AnnualSubmissionService annualSubmissionService;

    @Autowired
    private Environment environment;

    @Override
    public String getName() {
        return "AnnualSummaryDPFResolutionDataInitialiser";
    }

    @Override
    public int executionOrder() {
        return 999;
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void beforeInitialisation() {

    }

    @Override
    public void cleanupOldData() {

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
        if (environment.isTestEnvironment()) {
            List<AnnualSubmission> annualSubmissions = annualSubmissionService.getAnnualSubmissions(TEST_ORG_ID_1);
            AnnualSubmission annualSubmission = annualSubmissions.stream().filter(as -> as.getFinancialYear().equals(2018)).findFirst().orElse(null);
            if (annualSubmission != null) {
                annualSubmission.getBlocks().removeIf(b -> b.getGrantType().equals(AnnualSubmissionGrantType.DPF));
                annualSubmissionService.update(annualSubmission.getId(), annualSubmission);
            }
        }
    }

    @Override
    public void afterInitialisation() {
        Set<AnnualSubmission> allAnnualSubmissionsRequiringDPF = annualSubmissionService.findAllAnnualSubmissionsRequiringDPF();
        for (AnnualSubmission annualSubmission : allAnnualSubmissionsRequiringDPF) {
            boolean existingDPF = annualSubmission.getBlocks().stream().anyMatch(as -> AnnualSubmissionGrantType.DPF.equals(as.getGrantType()));
            if (existingDPF) {
                log.error("Found existing DPF entry");
            } else {
                annualSubmission.getBlocks().add(new AnnualSubmissionBlock(Actual, AnnualSubmissionGrantType.DPF));
                annualSubmission.getBlocks().add(new AnnualSubmissionBlock(AnnualSubmissionStatusType.Forecast, AnnualSubmissionGrantType.DPF));
                annualSubmissionService.update(annualSubmission.getId(), annualSubmission);
                log.info(String.format("Adding missing DPF entries for %d, year: %d", annualSubmission.getId(), annualSubmission.getFinancialYear()));
            }
        }
    }

}
