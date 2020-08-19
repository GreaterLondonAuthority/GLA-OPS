/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.di;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.di.DataInitialiserModule;

import javax.transaction.Transactional;

@Transactional
@Component
public class PaymentDataInitialiser implements DataInitialiserModule {

    public static final String TEST_INVALID_SAP_DATA_CONTENT = "Hello, world!";

    @Override
    public String getName() {
        return "Payment data initialiser";
    }


    @Override public void beforeInitialisation() {}

    @Override public void addReferenceData() {}

    @Override public void addUsers() {}

    @Override public void addOrganisations() {}

    @Override public void addTemplates() {
    }

    @Override public void addProgrammes() {
    }

    @Override public void addProjects() {
    }

    @Override
    public void addSupplementalData() {
    }

    @Override public void afterInitialisation() {}
}
