/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * State machine for GLA OPS projects from from multi assessment templates.
 */
@Component
public class MultiAssessmentProjectStateMachine extends ProjectStateMachine {

    Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Load allowed state transitions from CSV file on startup.
     */
    @Override
    @PostConstruct
    void loadAllowedTransitions() throws IOException {
        allowedTransitions = CSVFile.fromResource(this, StateModel.MultiAssessment.name() + ".csv").loadData(csvMapper);
        log.debug("Transitions loaded from CSV file");
    }

}
