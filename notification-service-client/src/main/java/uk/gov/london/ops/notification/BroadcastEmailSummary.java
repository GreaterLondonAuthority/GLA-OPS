/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification;

public class BroadcastEmailSummary {

    private String recipientEmail;
    private String recipientName;
    private String subheading;

    public BroadcastEmailSummary(String recipientEmail, String recipientName, String subheading) {
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.subheading = subheading;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getSubheading() {
        return subheading;
    }
}
