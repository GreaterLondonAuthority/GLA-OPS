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
import uk.gov.london.ops.framework.jpa.Join;
import uk.gov.london.ops.framework.jpa.JoinData;
import uk.gov.london.ops.framework.ComparableItem;
import uk.gov.london.ops.user.domain.User;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

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
    @ManyToOne(cascade = {})
    @JoinColumn(name = "created_by")
    private User creator;


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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getCreatorName() {
        return creator != null ? creator.getFullName() : null;
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
}
