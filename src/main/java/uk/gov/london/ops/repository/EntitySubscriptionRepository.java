/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.domain.user.EntitySubscription;

import java.util.List;

public interface EntitySubscriptionRepository extends JpaRepository<EntitySubscription, Integer> {

    List<EntitySubscription> findAllByEntityTypeAndEntityId(EntityType entityType, Integer entityId);

    Integer countByEntityTypeAndEntityId(EntityType entityType, Integer entityId);

    EntitySubscription findFirstByUsernameAndEntityTypeAndEntityId(String username, EntityType entityType, Integer entityId);

    Long deleteByUsernameAndEntityTypeAndEntityId(String username, EntityType entityType, Integer entityId);

}
