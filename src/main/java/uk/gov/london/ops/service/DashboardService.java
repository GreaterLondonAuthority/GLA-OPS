/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.FeatureStatus;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.NotFoundException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Service providing data for the user dashboard.
 */
@Service
public class DashboardService {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    UserService userService;

    @Autowired
    FeatureStatus featureStatus;

    /**
     * Return the dashboard metrics for the currently logged-in user.
     */
    public Map<String,Integer> getMetricsForCurrentUser() {
        if (userService.currentUser() == null) {
            throw new ForbiddenAccessException();
        }
        if (!featureStatus.isEnabled(FeatureStatus.Feature.Dashboard)) {
            throw new NotFoundException();
        }

        String username = userService.currentUser().getUsername();

        final Map<String,Integer> metrics = new TreeMap<>();

        RowCallbackHandler rowCallbackHandler = rs -> metrics.put(rs.getString("key"), rs.getInt("value"));

        jdbc.query("SELECT key, value FROM v_dashboard_metrics WHERE username = ?", rowCallbackHandler, username);

        return metrics;
    }
}
