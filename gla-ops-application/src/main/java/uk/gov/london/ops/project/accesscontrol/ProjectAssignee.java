/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.accesscontrol;


import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * Users assignment to a project
 */

@Entity (name = "project_assignee")
public class ProjectAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_assignee_seq_gen")
    @SequenceGenerator(name = "project_assignee_seq_gen", sequenceName = "project_assignee_seq",
            initialValue = 10000, allocationSize = 1)
    protected Integer id;

    @Column
    private Integer projectId;

    @Column
    private String username;

    @Column
    private String createdBy;

    @Column
    private OffsetDateTime createdOn;

    public ProjectAssignee() { }

    public ProjectAssignee(Integer projectId, String username) {
        this.projectId = projectId;
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }


}
