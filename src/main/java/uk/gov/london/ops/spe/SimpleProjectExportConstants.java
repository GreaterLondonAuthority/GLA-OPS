/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.spe;

/**
 * Created by chris on 23/02/2017.
 */
public class SimpleProjectExportConstants {

    public enum ReportPrefix {
        eg_, //Eligible grant
        ms_,//Milestone export prefix
        gs_,//Gran Source block prefix
        ds_,
        q_//Questions block prefix
    }


    public enum ReportSuffix {
        _date, //Milestone date suffix
        _status,//Milestone status suffix
        processing_route,
        _percentage//Milestone _percentage
    }

    public enum FieldNames {
        project_id,
        project_name,
        address,
        borough,
        post_code,
        description,
        programme_id,
        programme_name,
        template_id,
        template_name,
        bidding_arrangement,
        org_id,
        org_name,
        org_group_id,
        org_group_name,
        lead_org_id,
        lead_org_name
    }

}
