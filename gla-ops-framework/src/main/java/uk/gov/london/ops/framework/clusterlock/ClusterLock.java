/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.clusterlock;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "cluster_lock")
public class ClusterLock {

    public enum Type {
        EMAIL,
        SCHEDULED_NOTIFICATION
    }

    @Id
    @Column
    @Enumerated(EnumType.STRING)
    private Type lockType;

    @Column
    private String serverId;

    @Column
    private LocalDateTime expiryTime;

    public ClusterLock() {
    }

    public ClusterLock(Type lockType, String serverId, LocalDateTime expiryTime) {
        this.lockType = lockType;
        this.serverId = serverId;
        this.expiryTime = expiryTime;
    }

    public void updateLock(String serverId, LocalDateTime expiryTime) {
        this.serverId = serverId;
        this.expiryTime = expiryTime;
    }

    public Type getLockType() {
        return lockType;
    }

    public void setLockType(Type lockType) {
        this.lockType = lockType;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}
