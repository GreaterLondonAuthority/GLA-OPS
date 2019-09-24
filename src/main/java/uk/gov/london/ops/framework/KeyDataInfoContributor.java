/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.service.DashboardService;

/**
 * Provides key application data metrics for the System Dashboard.
 *
 * @author Steve Leach
 */
@Component
public class KeyDataInfoContributor implements InfoContributor {

    @Autowired
    DashboardService service;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("keyData", service.getSummaryOfKeyDataEntityCounts());
    }
}
