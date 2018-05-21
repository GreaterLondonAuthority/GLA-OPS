/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.organisation;

public enum OrganisationType {

    MANAGING_ORGANISATION (1, "Managing Organisation"),
    BOROUGH (2, "Borough"),
    PROVIDER (3, "Registered Provider"),
    OTHER (4, "Other"),
    TECHNICAL_SUPPORT (5, "Technical Support");

    private final int id;
    private final String summary;

    OrganisationType(int id, String summary) {
        this.id = id;
        this.summary = summary;
    }

    public int id() {
        return id;
    }

    public String summary() {
        return summary;
    }

    public static OrganisationType fromId(int id) {
        for (OrganisationType type: OrganisationType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

}
