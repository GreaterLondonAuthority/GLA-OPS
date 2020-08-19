/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.refdata.ConfigurableListItem;

import java.util.List;

public interface ConfigurableListItemRepository extends JpaRepository<ConfigurableListItem, Integer> {

    List<ConfigurableListItem> findAllByExternalIdOrderByDisplayOrder(Integer externalId);

}
