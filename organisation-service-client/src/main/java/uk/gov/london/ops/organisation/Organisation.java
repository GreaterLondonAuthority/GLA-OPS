/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation;

public interface Organisation {

    Integer GLA_OPS_ORG_ID = 8000;
    Integer GLA_CULTURE_ORG_ID = 8001;
    Integer GLA_REGEN_ORG_ID = 8002;
    Integer GLA_HNL_ORG_ID = 10000;

    Integer TEST_ORG_ID_1 = 9999;
    Integer TEST_ORG_ID_2 = 9998;
    Integer TEST_ORG_ID_3 = 9997;
    Integer TEST_ORG_ID_4 = 9996;
    Integer TEST_ORG_ID_5 = 9995;
    Integer TEST_ORG_ID_6 = 9994;
    Integer TEST_ORG_ID_7 = 9993;
    Integer TEST_ORG_ID_8 = 9992;
    Integer TEST_ORG_ID_9 = 9991;
    Integer TEST_ORG_ID_10 = 9970;
    Integer TEST_ORG_ID_11 = 9969;

    Integer getId();

    String getName();

    String getRegistrationKey();

    Integer getEntityType();

    OrganisationChangeStatusReason getChangeStatusReason();

    String getChangeStatusReasonDetails();

    Organisation getManagingOrganisation();

    boolean isManaging();

    boolean isInternalOrganisation();

    boolean isTeamOrganisation();

    Boolean getIsLearningProvider();

    boolean isAnnualReturnsEnabled();

    boolean isApproved();

    boolean isSkillsGatewayAccessAllowed();

    Integer getUkprn();

}
