/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.template.domain;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import uk.gov.london.ops.programme.domain.Programme;
import uk.gov.london.ops.programme.domain.ProgrammeTemplate;
import uk.gov.london.ops.project.state.StateModel;

@Entity
@Table(name = "template")
public class TemplateSummary implements Serializable {

    @Id
    private Integer id;

    @NotNull
    @Column(name = "name")
    private String name;

    @Column(name = "warning_message")
    private String warningMessage;

    @Column(name = "author")
    private String author;

    @Column(name = "template_status")
    @Enumerated(EnumType.STRING)
    private Template.TemplateStatus templateStatus;

    @Column(name = "state_model")
    @Enumerated(EnumType.STRING)
    private StateModel stateModel;

    @Column(name = "max_projects_for_template")
    private Integer numberOfProjectAllowedPerOrg;

    @Transient
    List<Programme> programmes;

    @Transient
    private ProgrammeTemplate.Status status;

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

    public String getWarningMessage() {
        return warningMessage;
    }

    public Template.TemplateStatus getTemplateStatus() {
        return templateStatus;
    }

    public void setTemplateStatus(Template.TemplateStatus templateStatus) {
        this.templateStatus = templateStatus;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public ProgrammeTemplate.Status getStatus() {
        return status;
    }

    public void setStatus(ProgrammeTemplate.Status status) {
        this.status = status;
    }

    public List<Programme> getProgrammes() {
        return programmes;
    }

    public void setProgrammes(List<Programme> programmes) {
        this.programmes = programmes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public StateModel getStateModel() {
        return stateModel;
    }

    public void setStateModel(StateModel stateModel) {
        this.stateModel = stateModel;
    }

    public Integer getNumberOfProjectAllowedPerOrg() {
        return numberOfProjectAllowedPerOrg;
    }

    public void setNumberOfProjectAllowedPerOrg(Integer numberOfProjectAllowedPerOrg) {
        this.numberOfProjectAllowedPerOrg = numberOfProjectAllowedPerOrg;
    }

    public static TemplateSummary createFrom(Template template) {
        TemplateSummary summary = new TemplateSummary();
        summary.setId(template.getId());
        summary.setName(template.getName());
        summary.setWarningMessage(template.getWarningMessage());
        summary.setAuthor(template.getAuthor());
        summary.setStateModel(template.getStateModel());
        summary.setNumberOfProjectAllowedPerOrg(template.getNumberOfProjectAllowedPerOrg());
        return summary;
    }


    public static TemplateSummary createFrom(ProgrammeTemplate programmeTemplate) {
        TemplateSummary summary = createFrom(programmeTemplate.getTemplate());
        summary.setStatus(programmeTemplate.getStatus());
        return summary;
    }


    public static List<TemplateSummary> createFrom(List<Template> templates) {
        return templates.stream().map(TemplateSummary::createFrom).collect(Collectors.toList());
    }

}
