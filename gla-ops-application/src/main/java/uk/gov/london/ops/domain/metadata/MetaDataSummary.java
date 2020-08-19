/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.metadata;

public class MetaDataSummary {

    private int numberOfUnreadNotifications;

    private String systemOutageMessage;

    public int getNumberOfUnreadNotifications() {
        return numberOfUnreadNotifications;
    }

    public void setNumberOfUnreadNotifications(int numberOfUnreadNotifications) {
        this.numberOfUnreadNotifications = numberOfUnreadNotifications;
    }

    public String getSystemOutageMessage() {
        return systemOutageMessage;
    }

    public void setSystemOutageMessage(String systemOutageMessage) {
        this.systemOutageMessage = systemOutageMessage;
    }

}
