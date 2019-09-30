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
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

/**
 * Created by chris on 26/06/2017.
 */
@Entity
public class Ward {


    @Id
    @NotNull
    private Integer id;

    @OrderBy
    @NotNull
    private Integer displayOrder;

    @Column(name = "name")
    @NotNull
    private String wardName;

    public Ward() {
    }

    public Ward(Integer id, Integer displayOrder, String wardName) {
        this.id = id;
        this.displayOrder = displayOrder;
        this.wardName = wardName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
