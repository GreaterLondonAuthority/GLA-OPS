/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain.template;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "risk_rating")
public class RiskRating implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "risk_rating_seq_gen")
    @SequenceGenerator(name = "risk_rating_seq_gen", sequenceName = "risk_rating_seq", initialValue = 10000, allocationSize = 1)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "color")
    private String color;

    @Column(name = "display_order")
    private Double displayOrder;

    public RiskRating() {
    }

    public RiskRating(String name, String description, String color, Double displayOrder) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.displayOrder = displayOrder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    protected RiskRating clone() {
        RiskRating clone = new RiskRating();
        clone.setName(this.getName());
        clone.setDescription(this.getDescription());
        clone.setColor(this.getColor());
        clone.setDisplayOrder(this.getDisplayOrder());
        return clone;
    }

}
