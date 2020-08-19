/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.domain;

/**
 * Created by chris on 19/09/2017.
 */
public class EntityCount {

    private Integer entityId;

    private Integer entityCount;

    public EntityCount() {
    }

    public EntityCount(Integer entityId, Integer entityCount) {
        this.entityId = entityId;
        this.entityCount = entityCount;
    }

    public EntityCount(Integer entityId, Long entityCount) {
        this.entityId = entityId;
        this.entityCount = entityCount.intValue();
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Integer getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(Integer entityCount) {
        this.entityCount = entityCount;
    }
}
