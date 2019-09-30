/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.domain.project.ComparableItem;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity(name="attachment")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="attachment_type" )
@DiscriminatorValue("STANDARD")
public class StandardAttachment implements ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "total_spend_attachment_seq_gen")
    @SequenceGenerator(name = "total_spend_attachment_seq_gen", sequenceName = "total_spend_attachment_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(name = "file_id")
    @JoinData(targetTable = "file", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The actual file (blob) referenced from this attached. ")
    private Integer fileId;

    public StandardAttachment() {}

    public StandardAttachment(Integer id, String fileName) {
        this.id = id;
        this.fileName = fileName;
    }

    public StandardAttachment(Integer id, String fileName, Integer fileId) {
        this.id = id;
        this.fileName = fileName;
        this.fileId = fileId;
    }

    public StandardAttachment(AttachmentFile file) {

        this.fileName = file.getFileName();
        this.fileId = file.getId();
        this.setCreatedOn(file.getCreatedOn());
        this.setCreator(file.getCreator());
    }

    public StandardAttachment copy() {
        final StandardAttachment target = new StandardAttachment();
        target.setFileName(this.getFileName());
        target.setCreatedOn(this.getCreatedOn());
        target.setCreator(this.getCreator());
        target.setFileId(this.getFileId());
        return target;
    }

    public Integer getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator != null ? creator.getFullName() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StandardAttachment that = (StandardAttachment) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return !(fileId != null ? !fileId.equals(that.fileId) : that.fileId != null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (fileId != null ? fileId.hashCode() : 0);
        return result;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(getFileId());
    }
}
