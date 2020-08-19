/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by chris on 21/08/2017.
 */
@Embeddable
public class ProgrammeTemplateID implements Serializable {

    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "programme_id")
    private Integer programmeId;

    public ProgrammeTemplateID() {
    }

    public ProgrammeTemplateID(Integer templateId, Integer programmeId) {
        this.templateId = templateId;
        this.programmeId = programmeId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public Integer getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(Integer programmeId) {
        this.programmeId = programmeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgrammeTemplateID that = (ProgrammeTemplateID) o;
        return Objects.equals(templateId, that.templateId) &&
                Objects.equals(programmeId, that.programmeId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(templateId, programmeId);
    }
}
