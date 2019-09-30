/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.project.SpendType;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.service.ManagedEntityInterface;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.jpa.NonJoin;

import javax.persistence.*;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.london.common.GlaUtils.csStringToList;
import static uk.gov.london.common.GlaUtils.listToCsString;

/**
 * A collection or summary of sub-programmes or projects that have been given common objective which meets an overall strategic aim.
 *
 * @author Steve Leach
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Programme implements Serializable, ManagedEntityInterface {

    public static final String SUPPORTED_REPORT_AFF_HSG = "AffordableHousing";

    public enum Status {
        Active, Archived, Abandoned
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "programme_seq_gen")
    @SequenceGenerator(name = "programme_seq_gen", sequenceName = "programme_seq", initialValue = 1000, allocationSize = 1)
    private Integer id;

    @NotNull
    @Column(name = "name")
    private String name;

    @JoinData(sourceTable = "programme", joinType = Join.JoinType.Complex, comment = "Inverse of join table relationship")
    @OneToMany(mappedBy = "programme", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProgrammeTemplate> templatesByProgramme = new HashSet<>();

    @Column(name = "restricted")
    private boolean restricted = false;

    @Column(name = "enabled")
    private boolean enabled = false;

    @Column(name = "in_assessment")
    private boolean inAssessment = false;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "modified_by")
    private User modifier;

    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    @Transient
    private Integer nbSubmittedProjects;

    @Column(name="financial_year")
    private Integer financialYear = null;


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


    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Set<Template> getTemplates() {
        return templatesByProgramme.stream().map(pt -> pt.getTemplate()).collect(Collectors.toSet());
    }


    @JsonIgnore
    public Set<ProgrammeTemplateAssessmentTemplate> getProgrammeTemplateAssessmentTemplates() {

        Set<ProgrammeTemplateAssessmentTemplate> assessmentTemplates = new HashSet<>();
        for (ProgrammeTemplate programmeTemplate : this.templatesByProgramme) {
            assessmentTemplates.addAll(programmeTemplate.getAssessmentTemplates());
        }
        return assessmentTemplates;
    }


    public Set<ProgrammeTemplate> getTemplatesByProgramme() {
        return templatesByProgramme;
    }

    public void setTemplatesByProgramme(Set<ProgrammeTemplate> templatesByProgramme) {
        this.templatesByProgramme = templatesByProgramme;
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

    public User getModifier() {
        return modifier;
    }

    public void setModifier(User modifier) {
        this.modifier = modifier;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Integer getNbSubmittedProjects() {
        return nbSubmittedProjects;
    }

    public void setNbSubmittedProjects(Integer nbSubmittedProjects) {
        this.nbSubmittedProjects = nbSubmittedProjects;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public Integer getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator != null ? creator.getFullName() : null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getModifierName() {
        return modifier != null ? modifier.getFullName() : null;
    }

    public Template getTemplate(String templateName) {
        for (ProgrammeTemplate templateProgramme: templatesByProgramme) {
            if (templateProgramme.getTemplate().getName().equals(templateName)) {
                return templateProgramme.getTemplate();
            }
        }
        return null;
    }

    public ProgrammeTemplate getProgrammeTemplateByTemplateID(Integer templateID) {
        return this.getTemplatesByProgramme().stream().filter(p -> p.getId().getTemplateId().equals(templateID)).findFirst().orElse(null);
    }

    public String getWbsCodeForTemplate(Integer templateId) {
        ProgrammeTemplate programmeTemplate = getProgrammeTemplate(templateId);
        return programmeTemplate != null ? programmeTemplate.getDefaultWbsCode() : null;
    }

    public String getWbsCodeForTemplate(Integer templateId, SpendType spendType) {
        ProgrammeTemplate programmeTemplate = getProgrammeTemplate(templateId);
        if (programmeTemplate != null) {
            if (SpendType.CAPITAL.equals(spendType)) {
                return programmeTemplate.getCapitalWbsCode();
            }

            if (SpendType.REVENUE.equals(spendType)) {
                return programmeTemplate.getRevenueWbsCode();
            }
        }

        return null;
    }

    public String getCeCodeForTemplate(Integer templateId) {
        ProgrammeTemplate programmeTemplate = getProgrammeTemplate(templateId);
        return programmeTemplate != null ? programmeTemplate.getCeCode() : null;
    }

    public String getRevenueWbsCodeForTemplate(Integer templateId) {
        ProgrammeTemplate programmeTemplate = getProgrammeTemplate(templateId);
        return programmeTemplate != null ? programmeTemplate.getRevenueWbsCode() : null;
    }

    private ProgrammeTemplate getProgrammeTemplate(Integer templateId) {
        return templatesByProgramme.stream().filter(t -> t.getId().getTemplateId().equals(templateId)).findFirst().orElse(null);
    }

    public boolean defaultWbsCodeSetForTemplate(Integer templateId) {
        ProgrammeTemplate programmeTemplate = getProgrammeTemplate(templateId);
        return programmeTemplate != null && programmeTemplate.getDefaultWbsCodeType() != null;
    }

    public void setWbsCodeForTemplate(Integer templateId, ProgrammeTemplate.WbsCodeType type, String wbsCode) {
        ProgrammeTemplate programmeTemplate = getProgrammeTemplate(templateId);
        if (programmeTemplate != null) {
            programmeTemplate.setWbsCode(type, wbsCode);
        }
        else {
            throw new ValidationException(String.format("Unable to find Template ID %d on Programme with ID %d", templateId, this.getId()));
        }
    }

    public void setCeCodeForTemplate(Integer templateId, String ceCode) {
        ProgrammeTemplate programmeTemplate = getProgrammeTemplate(templateId);
        if (programmeTemplate != null) {
            programmeTemplate.setCeCode(ceCode);
        }
        else {
            throw new ValidationException(String.format("Unable to find Template ID %d on Programme with ID %d", templateId, this.getId()));
        }
    }

    public void addTemplate(Template template)  {
        ProgrammeTemplate programmeTemplate = new ProgrammeTemplate(this, template);
        programmeTemplate.setId(new ProgrammeTemplateID(template.getId(), this.getId()));
        this.getTemplatesByProgramme().add(programmeTemplate);
    }

    public void addTemplates(Collection<Template> templates)  {
        for (Template template : templates) {
            this.addTemplate(template);
        }
    }


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Set<String> getGrantTypes() {
        Set<String> grantTypes = new HashSet<>();
        for (ProgrammeTemplate templateProgramme: templatesByProgramme) {
            if (templateProgramme.getTemplate() != null) {
                grantTypes.addAll(templateProgramme.getTemplate().getGrantTypes());
            }
        }
        return grantTypes;
    }

    public boolean hasIndicativeTemplate() {
        for (ProgrammeTemplate templateProgramme: templatesByProgramme) {
            if (templateProgramme.getTemplate().getIndicativeTenureConfiguration() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isTemplatePresent(Integer templateId) {
        if (templateId == null) {
            return false;
        }
        for (ProgrammeTemplate programmeTemplate : this.getTemplatesByProgramme()) {
            if (programmeTemplate.getId() != null && templateId.equals(programmeTemplate.getId().getTemplateId())) {
                return true;
            } else if (programmeTemplate.getTemplate() != null && templateId.equals(programmeTemplate.getTemplate().getId())) {
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
