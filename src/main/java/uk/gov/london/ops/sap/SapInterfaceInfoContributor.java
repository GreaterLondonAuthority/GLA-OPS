/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.sap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.service.finance.AuthorisedPaymentsProcessor;
import uk.gov.london.ops.service.finance.SapDataService;

import java.util.Map;
import java.util.TreeMap;

/**
 * Spring Boot Actuator InfoContributor for SAP interface.
 *
 * Adds details of the SAP interface to the response from the /sysops/info endpoint.
 */
@Component
public class SapInterfaceInfoContributor implements InfoContributor  {

    @Autowired
    @Qualifier("actualsSynchroniser")
    MoveItSynchroniser moveItSynchroniser;

    @Autowired
    AuthorisedPaymentsProcessor authorisedPaymentsProcessor;

    @Autowired
    SapDataService sapDataService;

    public void contribute(Info.Builder builder) {
        Map<String,Object> data = new TreeMap<>();

        data.put("moveItSyncPaused", moveItSynchroniser.isPaused());
        data.put("moveItSyncCount", moveItSynchroniser.getSyncCount());
        data.put("moveItLocalFiles", moveItSynchroniser.localFileCount());
        data.put("moveItLocalDir", moveItSynchroniser.getLocalDirectory());
        data.put("moveItRemoteDir", moveItSynchroniser.getRemoteDirectory());

        data.put("authPaymentEnabled", authorisedPaymentsProcessor.isEnabled());
        data.put("authPaymentExecutions", authorisedPaymentsProcessor.getExecutionCount());
        if (authorisedPaymentsProcessor.getLastExecuted() != null) {
            data.put("authPayLastRun", authorisedPaymentsProcessor.getLastExecuted().toString());
        }

        data.put("sapDataTotalRows", sapDataService.totalSapDataEntries());
        data.put("sapDataTotalErrors", sapDataService.errorCount());

        builder.withDetail("sapInterface", data);
    }
}
