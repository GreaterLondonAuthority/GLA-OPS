/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import uk.gov.london.ops.framework.enums.OrganisationContractStatus;

import java.util.Objects;

public class ContractActionDetails {
    private String text;
    private OrganisationContractStatus nextStatus;
    private Boolean doViewDetails;
    private boolean newVariationEntry;

    public ContractActionDetails() {

    }

    public ContractActionDetails(String text, OrganisationContractStatus nextStatus, Boolean doViewDetails, boolean newVariationEntry) {
        this.text = text;
        this.nextStatus = nextStatus;
        this.doViewDetails = doViewDetails;
        this.newVariationEntry = newVariationEntry;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OrganisationContractStatus getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(OrganisationContractStatus nextStatus) {
        this.nextStatus = nextStatus;
    }

    public Boolean getDoViewDetails() {
        return doViewDetails;
    }

    public void setDoViewDetails(Boolean doViewDetails) {
        this.doViewDetails = doViewDetails;
    }

    public boolean isNewVariationEntry() {
        return newVariationEntry;
    }

    public void setNewVariationEntry(boolean newVariationEntry) {
        this.newVariationEntry = newVariationEntry;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        // null check
        if (obj == null) {
            return false;
        }
        // type check and cast
        if (getClass() != obj.getClass()) {
            return false;
        }
        ContractActionDetails contractActionDetails = (ContractActionDetails) obj;
        return Objects.equals(text, contractActionDetails.text)
            && Objects.equals(nextStatus, contractActionDetails.nextStatus)
            && Objects.equals(doViewDetails, contractActionDetails.doViewDetails)
            && Objects.equals(newVariationEntry, contractActionDetails.newVariationEntry);
    }
}
