/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.refdata.CategoryValue;

import java.util.List;

public interface CategoryValueRepository extends JpaRepository<CategoryValue, Integer> {

    List<CategoryValue> findAllByCategoryOrderByDisplayOrder(CategoryValue.Category category);

    CategoryValue findByCategoryAndDisplayValue(CategoryValue.Category category, String displayValue);

}
