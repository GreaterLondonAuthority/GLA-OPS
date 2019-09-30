/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.report;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "legacy_ims_reported_figures")
public class LegacyImsReportedFigures implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "legacy_ims_reported_figures_seq_gen")
    @SequenceGenerator(name = "legacy_ims_reported_figures_seq_gen", sequenceName = "legacy_ims_reported_figures_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "scheme_id")
    private Integer schemeId;

    @Column(name = "programme")
    private String programme;

    @Column(name = "tenure_type")
    private String tenureType;

    @Column(name = "starts_achieved")
    private Integer startsAchieved;

    @Column(name = "completions_achieved")
    private Integer completionsAchieved;

    @Column(name = "sos_date")
    private String sosDate;

    @Column(name = "completion_date")
    private String completionDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(Integer schemeId) {
        this.schemeId = schemeId;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public String getTenureType() {
        return tenureType;
    }

    public void setTenureType(String tenureType) {
        this.tenureType = tenureType;
    }

    public Integer getStartsAchieved() {
        return startsAchieved;
    }

    public void setStartsAchieved(Integer startsAchieved) {
        this.startsAchieved = startsAchieved;
    }

    public Integer getCompletionsAchieved() {
        return completionsAchieved;
    }

    public void setCompletionsAchieved(Integer completionsAchieved) {
        this.completionsAchieved = completionsAchieved;
    }

    public String getSosDate() {
        return sosDate;
    }

    public void setSosDate(String sosDate) {
        this.sosDate = sosDate;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

}
