/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import uk.gov.london.ops.framework.enums.GrantType;

import javax.persistence.*;

@Entity(name = "PAYMENT_SOURCE")
public class PaymentSourceEntity {

    @Id
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column
    private GrantType grantType;

    @Column
    private boolean sendToSap = false;

    public PaymentSourceEntity() {
    }

    public PaymentSourceEntity(String name) {
        this.name = name;
    }

    public PaymentSourceEntity(String name, String description, GrantType grantType, boolean sendToSap) {
        this.name = name;
        this.description = description;
        this.grantType = grantType;
        this.sendToSap = sendToSap;
    }

    public boolean shouldPaymentSourceBeSentToSAP() {
        return this.sendToSap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(GrantType grantType) {
        this.grantType = grantType;
    }

    public boolean isSendToSap() {
        return sendToSap;
    }

    public void setSendToSap(boolean sendToSap) {
        this.sendToSap = sendToSap;
    }
}
