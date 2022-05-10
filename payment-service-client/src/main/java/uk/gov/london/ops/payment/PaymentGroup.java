/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import uk.gov.london.ops.notification.NotificationTargetEntity;

public interface PaymentGroup extends NotificationTargetEntity {

    Integer getId();

    Integer getProjectId();

}
