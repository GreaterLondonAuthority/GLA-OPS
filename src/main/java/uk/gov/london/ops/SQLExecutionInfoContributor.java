/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.DatabaseUpdate;
import uk.gov.london.ops.repository.DatabaseUpdateRepository;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;

import java.util.HashMap;
import java.util.Map;

/**
 * Summarises SQL Execution.
 *
 * Created by rbettison on 30/04/2018.
 */

@Configuration
@Component
public class SQLExecutionInfoContributor implements InfoContributor {

    @Autowired
    DatabaseUpdateRepository databaseUpdateRepository;

    public void contribute(Info.Builder builder) {
        builder.withDetail("sqlExecutionSummary", getSqlExecutionSummary());
    }

    public Map<String, Long> getSqlExecutionSummary() throws ForbiddenAccessException {
        Map<String, Long> updateSummary = new HashMap<>();
        updateSummary.put("pending", databaseUpdateRepository.countByStatus(DatabaseUpdate.Status.AwaitingApproval));
        updateSummary.put("approved", databaseUpdateRepository.countByStatus(DatabaseUpdate.Status.Approved));
        updateSummary.put("rejected", databaseUpdateRepository.countByStatus(DatabaseUpdate.Status.Rejected));
        return updateSummary;
    }
}
