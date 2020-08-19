/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import uk.gov.london.ops.framework.Environment;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Data initialiser. Runs in all environment to populate the database.
 *
 * The framework distinguishes between test environments and live-like environments,
 * and populates test environments with standard test data.
 *
 * This DataInitialiser framework class does not create any data itself, it simply
 * manages a set of DataInitialiserModule implementations that do the actual setup.
 */
public class DataInitialiser implements InfoContributor {

    Logger log = LoggerFactory.getLogger(getClass());

    // All the data initialiser module components get autowired into this array by Spring
    @Autowired DataInitialiserModule[] dataInitialiserModules;

    @Autowired protected Environment environment;

    protected boolean finished;

    protected int modulesExecuted = 0;    // Number of modules executed
    protected int stepCount = 0;          // Total number of steps executed across all modules
    protected int errors = 0;             // Total number of steps with errors

    /**
     * Entry point for the DataInitialiser framework.
     *
     * Executes all the DataInitialiserModule components, step-by-step.
     *
     * This should be the only @PostConstruct method used to initialise data in the database; the Spring
     * framework will call this method after the dependency injection framework has created an instance.
     */
    @PostConstruct
    public void initiliseEnvironmentData() {
        log.info("Starting data initialiser framework");

        runAllInitialiserModules();

        setFinished(true);

        log.info("Data initialiser framework complete");
    }

    /**
     * Returns true if all the steps of all the appropriate modules have been executed.
     */
    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * Runs all the steps in all the modules.
     */
    public void runAllInitialiserModules() {
        sortDataInitialiserModules();
        for (DataInitialiserModule.Step step : DataInitialiserModule.Step.values()) {
            runInitialiserStep(step);
        }
    }

    /**
     * Sorts the modules in their natural order, which is primarily based on their configured priority.
     */
    private void sortDataInitialiserModules() {
        log.debug("Sorting {} modules", dataInitialiserModules.length);
        Arrays.sort(dataInitialiserModules);
    }

    /**
     * Runs the specified step in all the modules.
     */
    public void runInitialiserStep(DataInitialiserModule.Step step) {
        for (DataInitialiserModule initialiser : dataInitialiserModules) {
            runInitialiserStep(initialiser, step);
        }
    }

    /**
     * Runs a specific step of a specific module.
     *
     * Only steps that are appropriate for the current environment are executed.
     *
     * Any exceptions are caught and logged, allowing the framework to proceed to the next module or step.
     */
    public void runInitialiserStep(DataInitialiserModule initialiser, DataInitialiserModule.Step step) {
        if (initialiser.runInAllEnvironments() || environment.initTestData()) {
            try {
                log.debug("Step {} of module {}", step.getSummary(), initialiser.getName());

                stepCount++;
                if (step.equals(DataInitialiserModule.Step.BEFORE)) {
                    modulesExecuted++;  // Count module execution at first step only
                }

                step.getModuleMethod().accept(initialiser);
            } catch (Exception e) {
                errors++;
                log.error("Error running step '" + step.getSummary() + "' of " + initialiser.getName(), e);
            }
        }
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> data = new TreeMap<>();
        data.put("modulesLoaded", dataInitialiserModules.length);
        data.put("modulesExecuted", modulesExecuted);
        data.put("stepsExecuted", stepCount);
        data.put("errors", errors);
        builder.withDetail("dataInitialiser", data);
    }
}
