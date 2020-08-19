/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.london.ops.user.domain.User;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
public class Message implements Serializable, OpsEntity<String> {

    public static final String coming_soon_message_key = "coming-soon";
    public static final String system_outage_message_key = "system-outage";
    public static final String home_page_message_key = "home-page";
    public static final String outputs_baseline_message_key = "outputs-baseline-message-key";
    public static final String project_label_message_key = "project-label";
    public static final String NOTIFICATION_RETURNED_BY_GLA_KEY = "RETURNED_BY_GLA";
    public static final String NOTIFICATION_APPROVED_FOR_NEXT_STAGE_KEY = "APPROVED_FOR_NEXT_STAGE";

    @Id
    @Column(nullable = false)
    private String code;

    @Column(name = "code_display_name", nullable = false)
    private String codeDisplayName;

    @Column(nullable = false)
    private String text;

    @Column
    private String createdBy;

    @Column
    private OffsetDateTime createdOn;

    @JsonIgnore
    @ManyToOne(cascade = {})
    @JoinColumn(name = "modified_by")
    private User modifiedByUser;

    @Column
    private OffsetDateTime modifiedOn;

    @Column
    private Boolean enabled;

    public Message() {}

    public Message(String code) {
        this.code = code;
    }

    public Message(String code, String text) {
        this(code);
        this.text = text;
    }

    public Message(String code, String displayName, String text, boolean enabled) {
        this(code, text);
        this.codeDisplayName = displayName;
        this.enabled = enabled;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getId() {
        return getCode();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCodeDisplayName() {
        return codeDisplayName;
    }

    public void setCodeDisplayName(String codeDisplayName) {
        this.codeDisplayName = codeDisplayName;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public User getModifiedByUser() {
        return modifiedByUser;
    }

    public void setModifiedByUser(User modifiedByUser) {
        this.modifiedByUser = modifiedByUser;
    }

    @Override
    public String getModifiedBy() {
        return modifiedByUser == null ? null : modifiedByUser.getUsername();
    }

    @Override
    public void setModifiedBy(String modifiedBy) {
        this.modifiedByUser = new User(modifiedBy);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getModifiedByName() {
        return modifiedByUser != null ? modifiedByUser.getFullName() : null;
    }

    @Override
    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    @Override
    public void setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}
