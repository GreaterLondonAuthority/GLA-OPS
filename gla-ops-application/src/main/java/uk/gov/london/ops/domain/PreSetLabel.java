/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.organisation.model.Organisation;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class PreSetLabel {

    public enum Status {
        Active, Inactive
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pre_set_label_seq_gen")
    @SequenceGenerator(name = "pre_set_label_seq_gen", sequenceName = "pre_set_label_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @NotNull
    @Column(name = "label_name")
    private String labelName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    @Column(name = "used")
    private boolean used;

    public PreSetLabel() {
    }

    public PreSetLabel(String labelName) {
        this.labelName = labelName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(
        Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    public Integer getManagingOrganisationId() {
        return managingOrganisation.getId();
    }

    public String getManagingOrganisationName() {
        return managingOrganisation.getName();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
