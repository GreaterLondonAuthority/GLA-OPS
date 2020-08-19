/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.internalblock;

import uk.gov.london.ops.project.risk.InternalRiskBlock;

public enum InternalBlockType {
    Risk("RISK", InternalRiskBlock.class),
    Assessment("ASSESSMENT", InternalAssessmentBlock.class),
    Questions("QUESTIONS", InternalQuestionsBlock.class);

    private final Class blockClass;
    private final String defaultName;

    InternalBlockType(String defaultName, Class blockClass) {
        this.defaultName = defaultName;
        this.blockClass = blockClass;
    }

    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Creates a new block instance matching the template block type.
     */
    public InternalProjectBlock newBlockInstance() {
        if (blockClass == null) {
            throw new IllegalStateException("Could not instantiate block: " + getDefaultName());
        } else {
            try {
                return (InternalProjectBlock) blockClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Error creating instance of" + blockClass.getName(), e);
            }
        }
    }
}