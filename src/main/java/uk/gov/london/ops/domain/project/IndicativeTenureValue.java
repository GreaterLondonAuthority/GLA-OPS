/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.project;

import javax.persistence.*;

/**
 * Created by chris on 28/11/2016.
 */
@Entity(name = "indicative_tenure_value")
public class IndicativeTenureValue implements Comparable, ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "indicative_val_seq_gen")
    @SequenceGenerator(name = "indicative_val_seq_gen", sequenceName = "indicative_val_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;


    @Column(name = "year")
    private Integer year;

    @Column(name = "units")
    private Integer units;

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

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public IndicativeTenureValue() {
    }

    public IndicativeTenureValue(Integer year, Integer units) {
        this.year = year;
        this.units = units;
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (o instanceof IndicativeTenureValue) {
            IndicativeTenureValue other = (IndicativeTenureValue) o;
            if (this.getYear() != null) {
                return this.getYear().compareTo(other.getYear());
            }
        }
        return 0;
    }

    public IndicativeTenureValue copy() {
        IndicativeTenureValue clone = new IndicativeTenureValue();
        clone.setUnits(this.units);
        clone.setYear(this.year);
        return clone;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(year);
    }
}
