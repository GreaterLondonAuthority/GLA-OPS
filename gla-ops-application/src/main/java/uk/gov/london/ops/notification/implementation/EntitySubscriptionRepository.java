/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.notification.implementation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.notification.EntitySubscription;

public interface EntitySubscriptionRepository extends JpaRepository<EntitySubscription, Integer> {

    List<EntitySubscription> findAllByEntityTypeAndEntityId(EntityType entityType, Integer entityId);

    Integer countByEntityTypeAndEntityId(EntityType entityType, Integer entityId);

    EntitySubscription findFirstByUsernameAndEntityTypeAndEntityId(String username, EntityType entityType, Integer entityId);

    Long deleteByUsernameAndEntityTypeAndEntityId(String username, EntityType entityType, Integer entityId);

    List<EntitySubscription> findAllByEntityTypeAndUsername(EntityType entityType, String username);

    @Query(value = "SELECT * FROM entity_subscription es INNER JOIN user_roles ur ON es.username = ur.username "
        + "WHERE es.entity_type = ?1 AND es.entity_id = ?2 AND ur.name IN ?3", nativeQuery = true)
    Set<EntitySubscription> findAllByEntityTypeAndEntityIdAndRoles(String entityType, Integer entityId, List<String> roles);

    default Set<String> getSubscribers(EntityType entityType, Integer entityId, List<String> roles) {
        return findAllByEntityTypeAndEntityIdAndRoles(entityType.name(), entityId, roles).stream()
            .map(EntitySubscription::getUsername).collect(Collectors.toSet());
    }

}
