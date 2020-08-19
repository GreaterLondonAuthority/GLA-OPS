/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.risk;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by chris on 21/08/2017.
 */
@Entity(name = "risk_level")
public class RiskLevelLookup {

    public enum RiskLevel {
        Low, Moderate, Significant
    }

    @EmbeddedId
    @JsonIgnore
    private RiskLevelID statusID;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private RiskLevel level;

    public RiskLevelLookup() {
    }

    public RiskLevelLookup(Integer impact, Integer probability, RiskLevel level) {
        this.statusID = new RiskLevelID(impact, probability);
        this.level = level;
    }

    public RiskLevelID getStatusID() {
        return statusID;
    }

    public void setStatusID(RiskLevelID statusID) {
        this.statusID = statusID;
    }

    public RiskLevel getLevel() {
        return level;
    }

    public void setLevel(RiskLevel level) {
        this.level = level;
    }
}
