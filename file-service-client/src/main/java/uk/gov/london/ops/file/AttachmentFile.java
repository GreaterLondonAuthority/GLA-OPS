/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;

@Entity(name = "file")
public class AttachmentFile implements Serializable, ComparableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_seq_gen")
    @SequenceGenerator(name = "file_seq_gen", sequenceName = "file_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_name")
    private String fileName;

    @Transient
    private byte[] fileContent;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private FileCategory category = FileCategory.Attachment;

    @Column(name = "created_on")
    private OffsetDateTime createdOn;

    @Column(name = "file_size")
    private Long fileSize;

    @JsonIgnore
    @Column(name = "created_by")
    private String creator;


    @Column(name = "organisation_id")
    @JoinData(targetTable = "organisation", targetColumn = "id", joinType = Join.JoinType.OneToOne,
            comment = "The organisation owning this attachment")
    private Integer organisationId;

    @JsonIgnore
    @JoinColumn(name = "storage_location")
    @Enumerated(EnumType.STRING)
    private StorageOption storageLocation = StorageOption.Database;

    @JsonIgnore
    @JoinColumn(name = "link")
    private String link;

    public AttachmentFile(String contentType, String fileName, byte[] fileContent, FileCategory category,
            OffsetDateTime createdOn, Long fileSize, String creator, Integer organisationId,
            StorageOption storageLocation, String link) {
        this.contentType = contentType;
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.category = category;
        this.createdOn = createdOn;
        this.fileSize = fileSize;
        this.creator = creator;
        this.organisationId = organisationId;
        this.storageLocation = storageLocation;
        this.link = link;
    }

    public AttachmentFile() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public FileCategory getCategory() {
        return category;
    }

    public void setCategory(FileCategory category) {
        this.category = category;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator;
    }

    public Integer getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Integer organisationId) {
        this.organisationId = organisationId;
    }

    @Override
    public String getComparisonId() {
        return String.valueOf(getId());
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public StorageOption getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(StorageOption storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        // null check
        if (obj == null) {
            return false;
        }
        // type check and cast
        if (getClass() != obj.getClass()) {
            return false;
        }
        AttachmentFile contractActionDetails = (AttachmentFile) obj;
        return Objects.equals(id, contractActionDetails.id)
            && Objects.equals(fileName, contractActionDetails.fileName)
            && Objects.equals(contentType, contractActionDetails.contentType)
            && Objects.equals(fileSize, contractActionDetails.fileSize)
            && Objects.equals(fileContent, contractActionDetails.fileContent);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((fileSize == null) ? 0 : fileSize.hashCode());
        result = prime * result + ((fileContent == null) ? 0 : fileContent.hashCode());
        return result;
    }
}
