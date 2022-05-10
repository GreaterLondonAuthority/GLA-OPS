/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.refdata.ConfigurableListItemEntity;
import uk.gov.london.ops.refdata.ConfigurableListItemType;

public interface ConfigurableListItemRepository extends JpaRepository<ConfigurableListItemEntity, Integer> {

    List<ConfigurableListItemEntity> findAllByExternalIdOrderByDisplayOrder(Integer externalId);

    List<ConfigurableListItemEntity> findAllByType(ConfigurableListItemType type);

    @Transactional
    void deleteAllByExternalId(Integer externalId);

}
