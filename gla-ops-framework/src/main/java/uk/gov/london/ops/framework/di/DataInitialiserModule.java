/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.di;

import java.util.function.Consumer;

/**
 * Interface for pluggable DataInitialiser modules.
 *
 * DataInitialiser code should be implemented (or refactored) as a set of classes each of which implements this interface, and
 * that is annotated as a Spring @Component.
 *
 * The new DataInitialiser framework finds all these components, and then calls each of the methods on this interface on each
 * component, in turn.
 *
 * Each method is run on all the components before moving on to the next method. This ensures that all templates are created
 * before all programmes, and all programmes before all projects, for example.
 */
public interface DataInitialiserModule extends Comparable<DataInitialiserModule> {

    /**
     * Steps for data initialiser modules.
     *
     * The steps will be executed in the order defined here.
     *
     * Each step is associated with a method that performs the step for the module implementation.
     */
    enum Step {
        BEFORE("before initialisation", DataInitialiserModule::beforeInitialisation),
        CLEANUP("cleanup old data", DataInitialiserModule::cleanupOldData),
        REF_DATA("add reference data", DataInitialiserModule::addReferenceData),
        ORGANISATIONS("add organisations", DataInitialiserModule::addOrganisations),
        USERS("add users", DataInitialiserModule::addUsers),
        TEMPLATES("add templates", DataInitialiserModule::addTemplates),
        PROGRAMMES("add programmes", DataInitialiserModule::addProgrammes),
        PROJECTS("add projects", DataInitialiserModule::addProjects),
        SUPPLEMENTAL("add supplemental data", DataInitialiserModule::addSupplementalData),
        AFTER("after initialisation", DataInitialiserModule::afterInitialisation);

        private final Consumer<DataInitialiserModule> method;
        private final String summary;

        Step(String summary, Consumer<DataInitialiserModule> method) {
            this.summary = summary;
            this.method = method;
        }

        public String getSummary() {
            return summary;
        }

        public Consumer<DataInitialiserModule> getModuleMethod() {
            return method;
        }

    }

    /**
     * The human-readable name of the module, set by each individual module.
     *
     * Must not be null.
     */
    String getName();

    /**
     * True if the module should run in all environments, false if only in environments with defined test data.
     */
    default boolean runInAllEnvironments() {
        return false;
    }

    default void beforeInitialisation() {
    }

    default void cleanupOldData() {
    }

    default void addReferenceData() {
    }

    default void addUsers() {
    }

    default void addOrganisations() {
    }

    default void addTemplates() {
    }

    default void addProgrammes() {
    }

    default void addProjects() {
    }

    default void addSupplementalData() {
    }

    default void afterInitialisation() {
    }

    /**
     * The execution order of the module.
     *
     * Lower numbers are executed before higher numbers; the default is 50.
     */
    default int executionOrder() {
        return 50;
    }

    /**
     * Compares two modules, for sorting purposes, primarily by execution order.
     */
    @Override
    default int compareTo(DataInitialiserModule other) {
        if (other == null) {
            return -1;
        }
        if (this.executionOrder() == other.executionOrder()) {
            return this.getClass().getName().compareTo(other.getClass().getName());
        }
        return this.executionOrder() - other.executionOrder();
    }
}
