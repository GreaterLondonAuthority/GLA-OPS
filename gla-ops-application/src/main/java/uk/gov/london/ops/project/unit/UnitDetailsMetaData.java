/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.unit;

import uk.gov.london.ops.project.template.domain.TemplateTenureType;
import uk.gov.london.ops.refdata.CategoryValue;

import java.util.List;

/**
 * USed by UI to show tenure/market rate relationships and other static data for page
 *
 * Created by chris on 30/05/2017.
 */
public class UnitDetailsMetaData {

    private List<CategoryValue> beds;
    private List<CategoryValue> unitDetails;
    private List<TemplateTenureType> tenureDetails;

    public UnitDetailsMetaData() {
    }

    public List<CategoryValue> getBeds() {
        return beds;
    }

    public void setBeds(List<CategoryValue> beds) {
        this.beds = beds;
    }

    public List<CategoryValue> getUnitDetails() {
        return unitDetails;
    }

    public void setUnitDetails(List<CategoryValue> unitDetails) {
        this.unitDetails = unitDetails;
    }

    public List<TemplateTenureType> getTenureDetails() {
        return tenureDetails;
    }

    public void setTenureDetails(List<TemplateTenureType> tenureDetails) {
        this.tenureDetails = tenureDetails;
    }

}
