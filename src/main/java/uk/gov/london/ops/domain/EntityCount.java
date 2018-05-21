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

    private Integer entityID;

    private Integer entityCount;

    public EntityCount() {
    }

    public EntityCount(Integer entityID, Integer entityCount) {
        this.entityID = entityID;
        this.entityCount = entityCount;
    }

    public EntityCount(Integer entityID, Long entityCount) {
        this.entityID = entityID;
        this.entityCount = entityCount.intValue();
    }

    public Integer getEntityID() {
        return entityID;
    }

    public void setEntityID(Integer entityID) {
        this.entityID = entityID;
    }

    public Integer getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(Integer entityCount) {
        this.entityCount = entityCount;
    }
}
