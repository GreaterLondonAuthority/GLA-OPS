/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by chris on 29/11/2016.
 */
@Entity(name="indicative_tenure_config")
public class IndicativeTenureConfiguration implements Serializable {

    public static final int MAX_NUMBER_OF_TENURE_YEARS = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicative_tenure_config_seq_gen")
    @SequenceGenerator(name = "indicative_tenure_config_seq_gen", sequenceName = "indicative_tenure_config_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;


    @Column(name = "indicative_tenure_start_year")
    private Integer indicativeTenureStartYear;

    @Column(name = "indicative_tenure_num_years")
    private Integer indicativeTenureNumberOfYears;

    @Column(name = "indicative_tenure_text_read_only")
    private String indicativeTenureTextReadOnly;

    @Column(name = "indicative_tenure_text_edit")
    private String indicativeTenureTextEdit;

    public IndicativeTenureConfiguration() {
    }

    public IndicativeTenureConfiguration(Integer indicativeTenureStartYear, Integer indicativeTenureNumberOfYears, String indicativeTenureTextReadOnly, String indicativeTenureTextEdit) {
        this.indicativeTenureStartYear = indicativeTenureStartYear;
        this.indicativeTenureNumberOfYears = indicativeTenureNumberOfYears;
        this.indicativeTenureTextReadOnly = indicativeTenureTextReadOnly;
        this.indicativeTenureTextEdit = indicativeTenureTextEdit;
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

    public String getIndicativeTenureTextReadOnly() {
        return indicativeTenureTextReadOnly;
    }

    public void setIndicativeTenureTextReadOnly(String indicativeTenureTextReadOnly) {
        this.indicativeTenureTextReadOnly = indicativeTenureTextReadOnly;
    }

    public String getIndicativeTenureTextEdit() {
        return indicativeTenureTextEdit;
    }

    public void setIndicativeTenureTextEdit(String indicativeTenureTextEdit) {
        this.indicativeTenureTextEdit = indicativeTenureTextEdit;
    }

}
