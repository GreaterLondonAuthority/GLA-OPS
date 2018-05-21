/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.user;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * An entry that is generated when a user requests a password reset.
 */
@Entity
public class PasswordResetToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "password_reset_token_seq_gen")
    @SequenceGenerator(name = "password_reset_token_seq_gen", sequenceName = "password_reset_token_seq", initialValue = 100, allocationSize = 1)
    private Integer id;

    @Column(name="token", nullable=false, updatable=false)
    private String token;

    @Column(name="username", nullable=false, updatable=false)
    private String username;

    @Column(name="expiry_date", nullable=false, updatable=false)
    private OffsetDateTime expiryDate;

    /** This is a flag that should be set to true if this token has been used to reset a user's password. */
    @Column(name="used", nullable=false)
    private boolean used = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public OffsetDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(OffsetDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
