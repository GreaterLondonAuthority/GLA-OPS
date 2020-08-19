/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.refdata.MarketType;

@Entity(name = "TemplateTenureTypeMarketType")
@Table(name = "template_tenure_type_market_type")
public class TemplateTenureTypeMarketType implements Serializable {

    @EmbeddedId
    private TemplateTenureTypeMarketTypeID id = new TemplateTenureTypeMarketTypeID();

    @JoinData(sourceTable = "template_market_type", sourceColumn = "market_type_id", targetTable = "market_type",
            targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "part of compound primary key.")
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("marketTypeId")
    private MarketType marketType;

    @JsonIgnore
    @JoinData(sourceTable = "template_market_type", sourceColumn = "template_tenure_type_id", targetTable = "template_tenure_type",
            targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "part of compound primary key.")
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("templateTenureTypeId")
    private TemplateTenureType templateTenureType;

    @Column(name = "market_type_name")
    private String marketTypeDisplayName;

    public TemplateTenureTypeMarketTypeID getId() {
        return id;
    }

    public void setId(TemplateTenureTypeMarketTypeID id) {
        this.id = id;
    }

    public MarketType getMarketType() {
        return marketType;
    }

    public void setMarketType(MarketType marketType) {
        this.marketType = marketType;
    }

    public String getMarketTypeDisplayName() {
        return marketTypeDisplayName;
    }

    public void setMarketTypeDisplayName(String marketTypeDisplayName) {
        this.marketTypeDisplayName = marketTypeDisplayName;
    }

    public TemplateTenureType getTemplateTenureType() {
        return templateTenureType;
    }

    public void setTemplateTenureType(TemplateTenureType templateTenureType) {
        this.templateTenureType = templateTenureType;
    }
}
