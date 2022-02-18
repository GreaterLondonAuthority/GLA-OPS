/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import java.io.Serializable;
import java.time.OffsetDateTime;

public interface OpsEntity<K> extends Serializable {

    K getId();

    OffsetDateTime getCreatedOn();

    void setCreatedOn(OffsetDateTime createdOn);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    default String getCreatorName() {
        return getCreatedBy();
    }

    default void setCreatorName(String creatorName) {
        setCreatedBy(creatorName);
    }

    OffsetDateTime getModifiedOn();

    void setModifiedOn(OffsetDateTime modifiedOn);

    String getModifiedBy();

    void setModifiedBy(String modifiedBy);

    default String getModifierName() {
        return getModifiedBy();
    }

    default void setModifierName(String modifierName) {
        setModifiedBy(modifierName);
    }

}
