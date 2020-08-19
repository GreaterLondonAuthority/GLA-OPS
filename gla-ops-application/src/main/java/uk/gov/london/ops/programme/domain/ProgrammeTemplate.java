/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.project.template.domain.Template;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "ProgrammeTemplate")
@Table(name = "programme_template")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProgrammeTemplate implements Serializable {

    public enum WbsCodeType {
        Revenue,
        Capital
    }

    public enum Status {
        Active,
        Inactive
    }

    @EmbeddedId
    private ProgrammeTemplateID id = new ProgrammeTemplateID();

    @JsonIgnore
    @JoinData(sourceTable = "programme_template", sourceColumn = "programme_id", targetTable = "programme", targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "part of compound primary key.")
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("programmeId")
    private Programme programme;

    @JsonIgnore
    @JoinData(sourceTable = "programme_template", sourceColumn = "template_id", targetTable = "template", targetColumn = "id", joinType = Join.JoinType.ManyToOne, comment = "part of compound primary key.")
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("templateId")
    private Template template;

    @Column(name = "template_capital_wbs_code")
    private String capitalWbsCode;

    @Column(name = "template_revenue_wbs_code")
    private String revenueWbsCode;

    @Column(name = "ce_code")
    private String ceCode;

    @Column(name = "default_wbs_code")
    @Enumerated(EnumType.STRING)
    private WbsCodeType defaultWbsCodeType;

    @Column(name = "payments_enabled")
    private boolean paymentsEnabled = true;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    @JoinData(sourceTable = "programme_template", targetTable = "programme_template_assessment_template",
            joinType = Join.JoinType.OneToMany, comment = "List of assessment templates for this programme template")
    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "programme_id", referencedColumnName = "programme_id"),
            @JoinColumn(name = "template_id", referencedColumnName = "template_id")
    })
    private Set<ProgrammeTemplateAssessmentTemplate> assessmentTemplates = new HashSet<>();

    public ProgrammeTemplate() {
    }

    public ProgrammeTemplate(Programme prog, Template template) {
        this.programme = prog;
        this.template = template;
        this.id = new ProgrammeTemplateID(template.getId(), prog.getId());
    }


    public ProgrammeTemplateID getId() {
        return id;
    }

    public void setId(ProgrammeTemplateID id) {
        this.id = id;
    }

    public Programme getProgramme() {
        return programme;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public String getCapitalWbsCode() {
        return capitalWbsCode;
    }

    public void setCapitalWbsCode(String capitalWbsCode) {
        this.capitalWbsCode = capitalWbsCode;
    }

    public String getTemplateName() {
        return template == null ? "" : template.getName();
    }

    public String getProgrammeName() {
        return programme == null ? "" : programme.getName();
    }

    public String getRevenueWbsCode() {
        return revenueWbsCode;
    }

    public void setRevenueWbsCode(String revenueWbsCode) {
        this.revenueWbsCode = revenueWbsCode;
    }

    public WbsCodeType getDefaultWbsCodeType() {
        return defaultWbsCodeType;
    }

    public void setDefaultWbsCodeType(WbsCodeType defaultWbsCodeType) {
        this.defaultWbsCodeType = defaultWbsCodeType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCeCode() {
        return ceCode;
    }

    public void setCeCode(String ceCode) {
        this.ceCode = ceCode;
    }

    public boolean isPaymentsEnabled() {
        return paymentsEnabled;
    }

    public void setPaymentsEnabled(boolean paymentsEnabled) {
        this.paymentsEnabled = paymentsEnabled;
    }

    public String getDefaultWbsCode() {
        String response = null;
        if (defaultWbsCodeType != null) {
            switch (defaultWbsCodeType) {
                case Capital:
                    response = getCapitalWbsCode();
                    break;
                case Revenue:
                    response = getRevenueWbsCode();
                    break;
            }
        }
        return response;
    }

    public void setWbsCode(WbsCodeType type, String wbsCode) {
        if (type != null) {
            switch (type) {
                case Capital:
                    setCapitalWbsCode(wbsCode);
                    break;
                case Revenue:
                    setRevenueWbsCode(wbsCode);
                    break;
            }
        }
    }

    public Set<ProgrammeTemplateAssessmentTemplate> getAssessmentTemplates() {
        return assessmentTemplates;
    }

    public void setAssessmentTemplates(Set<ProgrammeTemplateAssessmentTemplate> assessmentTemplates) {
        this.assessmentTemplates = assessmentTemplates;
    }

    public ProgrammeTemplateAssessmentTemplate getProgrammeTemplateAssessmentTemplateByAssessmentID(Integer assessmentID) {
        return this.getAssessmentTemplates().stream()
                .filter(a -> a.getAssessmentTemplate().getId().equals(assessmentID))
                .findFirst().orElse(null);
    }

}
