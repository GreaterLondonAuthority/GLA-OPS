/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 26/06/2017.
 */
@Entity(name = "borough")
public class BoroughEntity {

    @Id
    @NotNull
    private Integer id;

    @NotNull
    @OrderBy
    private Integer displayOrder;

    @Column(name = "name")
    @NotNull
    private String boroughName;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = WardEntity.class)
    @JoinColumn(name = "borough", nullable = false)
    private List<WardEntity> wards = new ArrayList<>();

    public BoroughEntity() {
    }

    public BoroughEntity(Integer id, Integer displayOrder, String boroughName) {
        this.id = id;
        this.displayOrder = displayOrder;
        this.boroughName = boroughName;
    }

    public BoroughEntity(Integer id, Integer displayOrder, String boroughName, List<WardEntity> wards) {
        this.id = id;
        this.displayOrder = displayOrder;
        this.boroughName = boroughName;
        this.wards = wards;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBoroughName() {
        return boroughName;
    }

    public void setBoroughName(String boroughName) {
        this.boroughName = boroughName;
    }

    public List<WardEntity> getWards() {
        return wards;
    }

    public void setWards(List<WardEntity> wards) {
        this.wards = wards;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
