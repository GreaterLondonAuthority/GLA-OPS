/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity(name = "template")
public class TemplateSummary implements Serializable {

    @Id
    private Integer id;

    @NotNull
    @Column(name = "name")
    private String name;

    @Column(name = "warning_message")
    private String warningMessage;

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

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public static TemplateSummary createFrom(Template template) {
        TemplateSummary summary = new TemplateSummary();
        summary.setId(template.getId());
        summary.setName(template.getName());
        summary.setWarningMessage(template.getWarningMessage());
        return summary;
    }

}
