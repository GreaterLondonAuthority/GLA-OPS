/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.internalblock;


import uk.gov.london.ops.project.template.domain.RiskRating;

import javax.persistence.*;

@Entity(name = "v_internal_block_summary")
public class InternalProjectBlockSummary {

    @Id
    private Integer id;
    private String blockDisplayName;
    @Enumerated(EnumType.STRING)
    private InternalBlockType type;
    private Integer displayOrder;
    private Integer projectId;
    private boolean show;

    @ManyToOne
    @JoinColumn(name = "rating_id")
    private RiskRating rating;

    public InternalProjectBlockSummary() {
    }

    public Integer getId() {
        return id;
    }

    public String getBlockDisplayName() {
        return blockDisplayName;
    }

    public InternalBlockType getType() {
        return type;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public RiskRating getRating() {
        return rating;
    }

    public void setRating(RiskRating rating) {
        this.rating = rating;
    }
}
