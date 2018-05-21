/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * Created by chris on 29/11/2016.
 */
@Entity(name="indicative_tenure_config")
public class IndicativeTenureConfiguration {

    public static final int MAX_NUMBER_OF_TENURE_YEARS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicative_tenure_config_seq_gen")
    @SequenceGenerator(name = "indicative_tenure_config_seq_gen", sequenceName = "indicative_tenure_config_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;


    @Column(name = "indicative_tenure_start_year")
    private Integer indicativeTenureStartYear;

    @Column(name = "indicative_tenure_num_years")
    private Integer indicativeTenureNumberOfYears;

    public IndicativeTenureConfiguration() {
    }

    public IndicativeTenureConfiguration(Integer indicativeTenureStartYear, Integer indicativeTenureNumberOfYears) {
        this.indicativeTenureStartYear = indicativeTenureStartYear;
        this.indicativeTenureNumberOfYears = indicativeTenureNumberOfYears;
    }



    public Integer getIndicativeTenureStartYear() {
        return indicativeTenureStartYear;
    }

    public void setIndicativeTenureStartYear(Integer indicativeTenureStartYear) {
        this.indicativeTenureStartYear = indicativeTenureStartYear;
    }

    public Integer getIndicativeTenureNumberOfYears() {
        return indicativeTenureNumberOfYears;
    }

    public void setIndicativeTenureNumberOfYears(Integer indicativeTenureNumberOfYears) {
        this.indicativeTenureNumberOfYears = indicativeTenureNumberOfYears;
    }
}
