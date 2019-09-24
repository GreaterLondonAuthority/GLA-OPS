/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import java.util.Objects;

public class FundingClaimPeriod {

    private Integer period;
    private String text;

    public FundingClaimPeriod() {}

    public FundingClaimPeriod(Integer period, String text) {
        this.period = period;
        this.text = text;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FundingClaimPeriod that = (FundingClaimPeriod) o;
        return Objects.equals(period, that.period) &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(period, text);
    }

}
