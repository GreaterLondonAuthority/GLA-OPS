/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryEntity;
import com.querydsl.core.annotations.QueryType;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;
import uk.gov.london.ops.organisation.model.OrganisationEntity;
import uk.gov.london.ops.project.template.domain.TemplateSummary;
import uk.gov.london.ops.service.ManagedEntityInterface;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.csStringToList;
import static uk.gov.london.common.GlaUtils.listToCsString;

@Entity(name = "programme")
@QueryEntity
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

    @Column(name = "in_assessment")
    private boolean inAssessment = false;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Programme.Status status = Programme.Status.Active;

    @JsonIgnore
    @QueryType(PropertyType.NONE)
    @JoinData(sourceTable = "programme", joinType = Join.JoinType.Complex, comment = "Inverse of join table relationship")
    @OneToMany(mappedBy = "programme", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProgrammeTemplate> templatesByProgramme = new HashSet<>();

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Transient
    private String modifierName;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "managing_organisation_id")
    private OrganisationEntity managingOrganisation;

    @JsonIgnore
    @Column(name = "supported_reports")
    private String supportedReportsString;

    @Column(name = "financial_year")
    private Integer financialYear;

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

    public boolean isInAssessment() {
        return inAssessment;
    }

    public void setInAssessment(boolean inAssessment) {
        this.inAssessment = inAssessment;
    }

    public Set<TemplateSummary> getTemplates() {
        return templatesByProgramme.stream().map(TemplateSummary::createFrom).collect(Collectors.toSet());
    }

    public Set<ProgrammeTemplate> getTemplatesByProgramme() {
        return templatesByProgramme;
    }

    public void setTemplatesByProgramme(Set<ProgrammeTemplate> templatesByProgramme) {
        this.templatesByProgramme = templatesByProgramme;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Programme.Status getStatus() {
        return status;
    }

    public void setStatus(Programme.Status status) {
        this.status = status;
    }

    @Override
    public OrganisationEntity getManagingOrganisation() {
        return managingOrganisation;
    }

    public void setManagingOrganisation(OrganisationEntity managingOrganisation) {
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

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public static ProgrammeSummary createFrom(Programme programme) {
        return createFrom(programme, false);
    }

    public static ProgrammeSummary createFrom(Programme programme, boolean includeTemplates) {
        ProgrammeSummary summary = new ProgrammeSummary(programme.getId(), programme.getName());
        summary.setRestricted(programme.isRestricted());
        summary.setEnabled(programme.isEnabled());
        summary.setManagingOrganisation(programme.getManagingOrganisation());
        summary.setFinancialYear(programme.getFinancialYear());
        if (includeTemplates) {
            summary.setTemplatesByProgramme(programme.getTemplatesByProgramme());
        }
        return summary;
    }

}
