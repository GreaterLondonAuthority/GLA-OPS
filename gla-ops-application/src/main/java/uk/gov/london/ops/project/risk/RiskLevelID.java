/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.risk;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by chris on 21/08/2017.
 */
@Embeddable
public class RiskLevelID implements Serializable {

    @Column(name = "impact", nullable = false)
    private Integer impact;

    @Column(name = "probability", nullable = false)
    private Integer probability;

    public RiskLevelID() {
    }

    public RiskLevelID(Integer impact, Integer probability) {
        this.impact = impact;
        this.probability = probability;
    }

    public Integer getImpact() {
        return impact;
    }

    public void setImpact(Integer impact) {
        this.impact = impact;
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }
}
