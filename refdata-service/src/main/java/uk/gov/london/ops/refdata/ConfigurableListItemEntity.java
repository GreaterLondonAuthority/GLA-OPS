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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * Created by chris on 16/02/2017.
 */
@Entity(name = "config_list_item")
public class ConfigurableListItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "config_list_item_seq_gen")
    @SequenceGenerator(name = "config_list_item_seq_gen", sequenceName = "config_list_item_seq",
            initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "external_id")
    private Integer externalId;

    @Column(name = "category")
    private String category;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ConfigurableListItemType type = ConfigurableListItemType.BudgetCategories;

    public ConfigurableListItemEntity() {
    }

    public ConfigurableListItemEntity(Integer externalId, String category, Integer displayOrder, ConfigurableListItemType type) {
        this.externalId = externalId;
        this.category = category;
        this.displayOrder = displayOrder;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public ConfigurableListItemType getType() {
        return type;
    }

    public void setType(ConfigurableListItemType type) {
        this.type = type;
    }
}
