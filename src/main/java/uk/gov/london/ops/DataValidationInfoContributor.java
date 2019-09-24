/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs data validation
 *
 * Created by rbettison on 13/03/2018.
 */

@Configuration
@Component
public class DataValidationInfoContributor implements InfoContributor {
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;


    public void contribute(Info.Builder builder) {

        Map<String, Object> dataValidation = new HashMap<>();
        dataValidation.put("validationFailures", validationFailures());
        builder.withDetail("dataValidation", dataValidation);

    }

    public List<ValidationFailure> validationFailures() {
        List<ValidationFailure> validationFailures = new ArrayList<>();

        validationFailures.addAll(getValidationFailuresByType(ValidationFailure.ValidationType.DuplicateBlocks, duplicateBlockCheck()));
        validationFailures.addAll(getValidationFailuresByType(ValidationFailure.ValidationType.TestValidation, testValidation()));

        return validationFailures;
    }

    public List<ValidationFailure> getValidationFailuresByType(ValidationFailure.ValidationType validationType, List<Map<String, Object>> details) {
        List<ValidationFailure> validationFailures = new ArrayList<>();
        for(Map<String, Object> detail : details) {
            validationFailures.add(new ValidationFailure(validationType, detail.toString()));
        }
        return validationFailures;
    }

    public List<Map<String, Object>> duplicateBlockCheck() {

        List<Map<String, Object>> duplicateProjectBlocks = jdbcTemplate.queryForList("" +
                "select project_id as projectId " +
                "from project_block " +
                "where latest_version = true " +
                "and latest_for_project is not null " +
                "group by project_id, block_type, display_order " +
                "having count(project_id) > 1 " +
                "order by count(project_id) DESC");
        log.debug("Project block duplicate check done.");
        return duplicateProjectBlocks;

    }

    public List<Map<String,Object>> testValidation() {

        //look for DI project for use in testing envs
        List<Map<String, Object>> testValidation = jdbcTemplate.queryForList("select project_id as projectId from v_project_details" +
                " where project_title = 'Active Auto Approval' " +
                "or project_title = 'Auto Approval Project'");
        return testValidation;

    }
}
