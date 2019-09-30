/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service.project;

import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.payment.PaymentGroup;

/**
 * Interface to be implemented by services that can generate payments for projects when payment authorisation is requested.
 */
public interface ProjectPaymentGenerator {

    /**
     * Method called when payment authorisation is requested on a project, this will result in a pending payment creation.
     * @param project
     * @param approvalRequestedBy
     * @return the created payment group
     */
    PaymentGroup generatePaymentsForProject(Project project, String approvalRequestedBy);

}
