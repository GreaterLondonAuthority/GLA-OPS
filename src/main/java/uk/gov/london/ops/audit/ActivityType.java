/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.audit;

public enum ActivityType {

    Add("added"),
    Update("updated"),
    Delete("deleted"),
    StartEdit("editing started"),
    StopEdit("editing stopped"),
    Requested("requested"),
    Approved("approved"),
    Declined("declined");

    private final String verb;

    ActivityType(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb;
    }

}
