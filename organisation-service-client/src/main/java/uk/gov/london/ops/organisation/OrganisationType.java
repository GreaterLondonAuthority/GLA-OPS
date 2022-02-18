/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrganisationType {

    MANAGING_ORGANISATION(1, "Managing Organisation", 13, false, true),
    LOCAL_AUTHORITY(2, "Local Authority", 12, true, false),
    PROVIDER(3, "Registered Provider", 16, true, false, true),
    OTHER(4, "Other", 22, false, false),
    TECHNICAL_SUPPORT(5, "Technical Support", 20, false, false),
    LEARNING_PROVIDER(6, "Learning Provider", 9, false, false, true),
    SMALL_BUSINESS(7, "Small Business", 18, false, false, true),
    TEAM(8, "Team", 19, false, false),
    CHARITABLE_INCORPORATED_ORG(9, "Charitable Incorporated Organisation", 1, false, false),
    REGISTERED_SOCIETY(10, "Registered Society (including Cooperative Societies and Community Benefit Societies)", 17, false, false),
    COMMUNITY_GUARANTEE(11, "Community Interest Company Limited by Guarantee", 3, false, false),
    COMMUNITY_SHARES(12, "Community Interest Company Limited by Shares", 4, false, false),
    COMPANY_LIMITED_GUARANTEE(13, "Company Limited by Guarantee (including Registered Charities/Specialist Designated Institutions)", 5, false, false),
    COMPANY_LIMITED_SHARES(14, "Company Limited by Shares", 6, false, false),
    COOPERATIVE_SOCIETY(15, "Co-operative Society", 7, false, false, true),
    EDUCATIONAL(16, "Educational body (excluding Local Authorities/Limited Companies/Specialist Designated Institutions)", 8, false, false),
    LIMITED_LIABILITY(17, "Limited Liability Partnership", 10, false, false),
    LIMITED_PARTNERSHIP(18, "Limited Partnership", 11, false, false),
    PARTNERSHIP(19, "Partnership (Unlimited)", 14, false, false),
    PUBLIC_LIMITED_COMPANY(20, "Public Limited Company", 15, false, false),
    UNINCORPORATED_ASSOCIATION(21, "Unincorporated Association", 21, false, false);

    private final int id;
    private final String summary;
    private final int displayOrder;
    private final boolean annualReturnsEnabled;
    private final boolean internal;
    /** Set to true for legacy types that we dont want to use going forwards. */
    private final boolean deprecated;

    OrganisationType(int id, String summary, int displayOrder, boolean annualReturnsEnabled, boolean internal) {
        this(id, summary, displayOrder, annualReturnsEnabled, internal, false);
    }

    OrganisationType(int id, String summary, int displayOrder, boolean annualReturnsEnabled, boolean internal, boolean deprecated) {
        this.id = id;
        this.summary = summary;
        this.displayOrder = displayOrder;
        this.annualReturnsEnabled = annualReturnsEnabled;
        this.internal = internal;
        this.deprecated = deprecated;
    }

    public int getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isAnnualReturnsEnabled() {
        return annualReturnsEnabled;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public static OrganisationType fromId(int id) {
        for (OrganisationType type: OrganisationType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public static List<Integer> getInternalOrganisationTypesIds() {
        return Arrays.stream(values())
                .filter(ot -> ot.internal).map(organisationType -> organisationType.id)
                .collect(Collectors.toList());
    }
}
