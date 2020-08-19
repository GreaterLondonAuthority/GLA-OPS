/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity(name = "indicative_tenure_config_year")
public class TenureYear implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicative_tenure_config_year_seq_gen")
    @SequenceGenerator(name = "indicative_tenure_config_year_seq_gen", sequenceName = "indicative_tenure_config_year_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "year")
    private Integer year;

    @Column(name = "external_id")
    private Integer externalId;

    @Column(name = "tariff_rate")
    private Integer tariffRate;

    public TenureYear() {
    }

    public TenureYear(Integer year, Integer externalId, Integer tariffRate) {
        this.year = year;
        this.externalId = externalId;
        this.tariffRate = tariffRate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public Integer getTariffRate() {
        return tariffRate;
    }

    public void setTariffRate(Integer tariffRate) {
        this.tariffRate = tariffRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TenureYear that = (TenureYear) o;
        return Objects.equals(year, that.year)
                && Objects.equals(externalId, that.externalId)
                && Objects.equals(tariffRate, that.tariffRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, externalId, tariffRate);
    }

}
