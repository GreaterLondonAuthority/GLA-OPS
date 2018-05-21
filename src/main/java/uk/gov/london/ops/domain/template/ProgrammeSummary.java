/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.util.jpajoins.NonJoin;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.ops.util.GlaOpsUtils.csStringToList;
import static uk.gov.london.ops.util.GlaOpsUtils.listToCsString;

@Entity(name = "programme")
@NonJoin("Summary entity, does not provide join information")
public class ProgrammeSummary implements ManagedEntityInterface {

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "restricted")
    private boolean restricted = false;

    @Column(name = "enabled")
    private boolean enabled = false;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {})
    @JoinTable(name = "programme_template",
            joinColumns = @JoinColumn(name = "programme_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "template_id", referencedColumnName = "id"))
    private Set<TemplateSummary> templates;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @JsonIgnore
    @Column(name = "supported_reports")
    private String supportedReportsString;

    public ProgrammeSummary() {}

    public ProgrammeSummary(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<TemplateSummary> getTemplates() {
        return templates;
    }

    public void setTemplates(Set<TemplateSummary> templates) {
        this.templates = templates;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator != null ? creator.getFullName() : null;
    }

    @Override
    public Organisation getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(Organisation managingOrganisation) {
        this.managingOrganisation = managingOrganisation;
    }

    public String getSupportedReportsString() {
        return supportedReportsString;
    }

    public void setSupportedReportsString(String supportedReportsString) {
        this.supportedReportsString = supportedReportsString;
    }

    public List<String> getSupportedReports() {
        return csStringToList(supportedReportsString);
    }

    public void setSupportedReports(List<String> supportedReports) {
        this.supportedReportsString = listToCsString(supportedReports);
    }

    public static ProgrammeSummary createFrom(Programme programme) {
        return createFrom(programme, false);
    }

    public static ProgrammeSummary createFrom(Programme programme, boolean includeTemplates) {
        ProgrammeSummary summary = new ProgrammeSummary(programme.getId(), programme.getName());
        summary.setRestricted(programme.isRestricted());
        summary.setEnabled(programme.isEnabled());
        summary.setManagingOrganisation(programme.getManagingOrganisation());
        if (includeTemplates) {
            summary.setTemplates(programme.getTemplates().stream().map(TemplateSummary::createFrom).collect(Collectors.toSet()));
        }
        return summary;
    }

}
