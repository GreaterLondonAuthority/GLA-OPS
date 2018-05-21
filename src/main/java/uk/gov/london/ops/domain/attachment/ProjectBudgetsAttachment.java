/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.attachment;

import uk.gov.london.ops.domain.project.ComparableItem;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("PROJECT_BUDGETS")
public class ProjectBudgetsAttachment extends StandardAttachment implements ComparableItem {

    @Column(name = "document_type")
    private String documentType;


    public ProjectBudgetsAttachment() {
    }

    public ProjectBudgetsAttachment(Integer id, String fileName) {
        super(id, fileName);
    }

    public ProjectBudgetsAttachment(Integer id, String fileName, Integer fileId) {
        super(id, fileName, fileId);
    }

    public ProjectBudgetsAttachment copy() {
        final ProjectBudgetsAttachment target = new ProjectBudgetsAttachment();
        target.setDocumentType(this.getDocumentType());
        target.setFileName(this.getFileName());
        target.setCreatedOn(this.getCreatedOn());
        target.setCreator(this.getCreator());
        target.setFileId(this.getFileId());
        return target;
    }


    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

}
