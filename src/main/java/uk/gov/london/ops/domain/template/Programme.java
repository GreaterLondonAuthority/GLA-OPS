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
import org.apache.commons.lang3.StringUtils;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.util.jpajoins.NonJoin;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.london.ops.util.GlaOpsUtils.csStringToList;
import static uk.gov.london.ops.util.GlaOpsUtils.listToCsString;

/**
 * A collection or summary of sub-programmes or projects that have been given common objective which meets an overall strategic aim.
 *
 * @author Steve Leach
 */
@Entity
public class Programme implements Serializable, ManagedEntityInterface {

    public static final String SUPPORTED_REPORT_AFF_HSG = "AffordableHousing";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "programme_seq_gen")
    @SequenceGenerator(name = "programme_seq_gen", sequenceName = "programme_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @NotNull
    @Column(name = "name")
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {})
    @JoinTable(name = "programme_template",
            joinColumns = @JoinColumn(name = "programme_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "template_id", referencedColumnName = "id"))
    private Set<Template> templates;

    @Column(name = "restricted")
    private boolean restricted = false;

    @Column(name = "enabled")
    private boolean enabled = false;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name="wbs_code")
    @NonJoin("SAP Payment code")
    private String wbsCode;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private Organisation managingOrganisation;

    @JsonIgnore
    @Column(name = "supported_reports")
    private String supportedReportsString;

    @Transient
    private Integer nbSubmittedProjects;


    public Programme() {
        // empty
    }

    public Programme(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(Set<Template> templates) {
        this.templates = templates;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Integer getNbSubmittedProjects() {
        return nbSubmittedProjects;
    }

    public void setNbSubmittedProjects(Integer nbSubmittedProjects) {
        this.nbSubmittedProjects = nbSubmittedProjects;
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator != null ? creator.getFullName() : null;
    }

    public Template getTemplate(String templateName) {
        for (Template template: templates) {
            if (template.getName().equals(templateName)) {
                return template;
            }
        }
        return null;
    }

    public String getWbsCode() {
        return wbsCode;
    }

    public void setWbsCode(String wbsCode) {
        this.wbsCode = wbsCode;
    }

    public boolean hasWbsCode() {
        return !StringUtils.isEmpty(wbsCode);
    }

    public Set<String> getGrantTypes() {
        Set<String> grantTypes = new HashSet<>();
        for (Template template: templates) {
            grantTypes.addAll(template.getGrantTypes());
        }
        return grantTypes;
    }

    public boolean hasIndicativeTemplate() {
        for (Template template: templates) {
            if (template.getIndicativeTenureConfiguration() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Programme programme = (Programme) o;

        return !(id != null ? !id.equals(programme.id) : programme.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
