/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import uk.gov.london.ops.domain.project.GrantType;

public enum PaymentSource {

    Grant,
    DPF,
    RCGF,
    ESF,
    MOPAC;

    public boolean shouldPaymentSourceBeSentToSAP() {
        return Grant.equals(this) || ESF.equals(this);
    }

    public GrantType getGrantType() {
        switch (this) {
            case RCGF:
                return GrantType.RCGF;
            case DPF:
                return GrantType.DPF;
            default:
                return GrantType.Grant;
        }
    }



}
