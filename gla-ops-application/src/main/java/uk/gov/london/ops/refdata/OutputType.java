/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by chris on 16/02/2017.
 */
@Entity(name = "output_type")
public class OutputType {

    @Id()
    @Column(name = "key")
    private String key;

    @Column(name = "description")
    private String description;

    public OutputType() {
    }

    public OutputType(String key) {
        this.key = key;
    }

    public OutputType(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
