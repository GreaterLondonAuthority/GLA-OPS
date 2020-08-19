/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class FundingClaimsTotals {

    private BigDecimal actualTotal;
    private BigDecimal forecastTotal;
    private BigDecimal deliveryTotal;

    // contract type
    private BigDecimal contractValueTotal;
    private BigDecimal flexibleTotal;
    private final Set<ContractTypeTotal> contractTypeTotals = new HashSet<>();

    public FundingClaimsTotals() {
    }

    FundingClaimsTotals(BigDecimal actualTotal, BigDecimal forecastTotal, BigDecimal deliveryTotal) {
        this.actualTotal = actualTotal;
        this.forecastTotal = forecastTotal;
        this.deliveryTotal = deliveryTotal;
    }

    public void addContractTypeTotal(String total, BigDecimal funding, BigDecimal flexibleFunding) {
        this.contractTypeTotals.add(new ContractTypeTotal(total, funding, flexibleFunding));
    }

    public BigDecimal getActualTotal() {
        return actualTotal;
    }

    public void setActualTotal(BigDecimal actualTotal) {
        this.actualTotal = actualTotal;
    }

    public BigDecimal getForecastTotal() {
        return forecastTotal;
    }

    public void setForecastTotal(BigDecimal forecastTotal) {
        this.forecastTotal = forecastTotal;
    }

    public BigDecimal getDeliveryTotal() {
        return deliveryTotal;
    }

    public void setDeliveryTotal(BigDecimal deliveryTotal) {
        this.deliveryTotal = deliveryTotal;
    }

    public BigDecimal getContractValueTotal() {
        return contractValueTotal;
    }

    public void setContractValueTotal(BigDecimal contractValueTotal) {
        this.contractValueTotal = contractValueTotal;
    }

    public BigDecimal getFlexibleTotal() {
        return flexibleTotal;
    }

    public void setFlexibleTotal(BigDecimal flexibleTotal) {
        this.flexibleTotal = flexibleTotal;
    }

    public Set<ContractTypeTotal> getContractTypeTotals() {
        return contractTypeTotals;
    }

    public BigDecimal getPercentage() {
        if (flexibleTotal != null && contractValueTotal != null
                && contractValueTotal.compareTo(BigDecimal.ZERO) != 0 && flexibleTotal.compareTo(BigDecimal.ZERO) != 0) {
            return flexibleTotal.multiply(new BigDecimal(100)).divide(contractValueTotal, 0, BigDecimal.ROUND_UP);
        }
        return null;
    }

    public class ContractTypeTotal {

        private String contractType;
        private BigDecimal funding;
        private BigDecimal flexibleFunding;

        public ContractTypeTotal() {
        }

        public ContractTypeTotal(String contractType, BigDecimal funding, BigDecimal flexibleFunding) {
            this.contractType = contractType;
            this.funding = funding;
            this.flexibleFunding = flexibleFunding;
        }

        public String getContractType() {
            return contractType;
        }

        public BigDecimal getFunding() {
            return funding;
        }

        public BigDecimal getFlexibleFunding() {
            return flexibleFunding;
        }

        public BigDecimal getPercentage() {
            if (funding != null && flexibleFunding != null && funding.compareTo(BigDecimal.ZERO) != 0
                    && flexibleFunding.compareTo(BigDecimal.ZERO) != 0) {
                return flexibleFunding.multiply(BigDecimal.valueOf(100)).divide(funding, 0, BigDecimal.ROUND_UP);
            }
            return null;
        }
    }
}
