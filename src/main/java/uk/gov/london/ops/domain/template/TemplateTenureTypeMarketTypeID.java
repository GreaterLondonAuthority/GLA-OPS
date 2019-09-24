/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by chris on 21/08/2017.
 */
@Embeddable
public class TemplateTenureTypeMarketTypeID implements Serializable {

    @Column(name = "template_tenure_type_id")
    private Integer templateTenureTypeId    ;

    @Column(name = "market_type_id")
    private Integer marketTypeId;

    public TemplateTenureTypeMarketTypeID() {
    }

    public TemplateTenureTypeMarketTypeID(Integer templateId, Integer templateTenureTypeId) {
        this.templateTenureTypeId = templateId;
        this.marketTypeId = marketTypeId;
    }

    public Integer getMarketTypeId() {
        return marketTypeId;
    }

    public void setMarketTypeId(Integer marketTypeId) {
        this.marketTypeId = marketTypeId;
    }

    public Integer getTemplateTenureTypeId() {
        return templateTenureTypeId;
    }

    public void setTemplateTenureTypeId(Integer templateTenureTypeId) {
        this.templateTenureTypeId = templateTenureTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateTenureTypeMarketTypeID that = (TemplateTenureTypeMarketTypeID) o;
        return Objects.equals(templateTenureTypeId, that.templateTenureTypeId) &&
                Objects.equals(marketTypeId, that.marketTypeId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(templateTenureTypeId, marketTypeId);
    }
}
