/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user.domain;

import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_org_finance_threshold")
public class UserOrgFinanceThreshold {

    @EmbeddedId
    @JoinData(joinType = Join.JoinType.Complex, sourceTable = "user_org_finance_threshold",
            comment = "compound pk join to user/organisation")
    UserOrgKey id;

    @Column
    private Long approvedThreshold;

    @Column
    private Long pendingThreshold;

    @Column
    @JoinData(joinType = Join.JoinType.ManyToOne, targetColumn = "username", targetTable = "users",
            comment = "")
    private String requesterUsername;

    @Column
    @JoinData(joinType = Join.JoinType.ManyToOne, targetColumn = "username", targetTable = "users",
            comment = "")
    private String approverUsername;

    public UserOrgFinanceThreshold() {
    }

    public UserOrgFinanceThreshold(String userName, Integer organisationId) {
        id = new UserOrgKey();
        id.setUsername(userName);
        id.setOrganisationId(organisationId);
        setApprovedThreshold(0L);
    }

    public UserOrgFinanceThreshold(String userName, Integer organisationId, Long approvedThreshold, Long pendingThreshold, String requesterUsername, String approverUsername) {
        this.id = new UserOrgKey();
        id.setUsername(userName);
        id.setOrganisationId(organisationId);
        this.approvedThreshold = approvedThreshold;
        this.pendingThreshold = pendingThreshold;
        this.requesterUsername = requesterUsername;
        this.approverUsername = approverUsername;
    }

    public UserOrgKey getId() {
        return id;
    }

    public void setId(UserOrgKey id) {
        this.id = id;
    }

    public Long getApprovedThreshold() {
        return approvedThreshold;
    }

    public void setApprovedThreshold(Long approvedThreshold) {
        this.approvedThreshold = approvedThreshold;
    }

    public Long getPendingThreshold() {
        return pendingThreshold;
    }

    public void setPendingThreshold(Long pendingThreshold) {
        this.pendingThreshold = pendingThreshold;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getApproverUsername() {
        return approverUsername;
    }

    public void setApproverUsername(String approverUsername) {
        this.approverUsername = approverUsername;
    }

    public boolean isPending() {
        return pendingThreshold != null;
    }


    public void clear() {
        setApprovedThreshold(null);
        setPendingThreshold(null);
        setRequesterUsername(null);
        setApproverUsername(null);
    }

}
