/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.block;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.user.domain.UserEntity;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Created by chris on 06/01/2017.
 */
@Entity(name = "lock_details")
public class LockDetails {

    @Id()
    private Integer id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "id")
    @JsonIgnore
    private NamedProjectBlock block;

    @Column(name = "project_id")
    @JoinData(targetTable = "project", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The project locked by this entity")
    private Integer projectId;

    @Column(name = "username")
    private String username;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "lock_timeout_time")
    private OffsetDateTime lockTimeoutTime;

    public LockDetails() {
    }

    public LockDetails(UserEntity user, int timeoutInMinutes) {
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.lockTimeoutTime = OffsetDateTime.now().plusMinutes(timeoutInMinutes);
    }

    public NamedProjectBlock getBlock() {
        return block;
    }

    public void setBlock(NamedProjectBlock block) {
        this.block = block;
        this.projectId = block.getProjectId();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public OffsetDateTime getLockTimeoutTime() {
        return lockTimeoutTime;
    }

    public void setLockTimeoutTime(OffsetDateTime lockTimeoutTime) {
        this.lockTimeoutTime = lockTimeoutTime;
    }

    @JsonIgnore
    public Integer getProjectId() {
        return projectId;
    }

    public Integer getId() {
        return id;
    }
}
