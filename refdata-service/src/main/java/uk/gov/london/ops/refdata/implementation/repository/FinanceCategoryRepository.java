/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.refdata.FinanceCategoryEntity;

@Repository
public interface FinanceCategoryRepository extends JpaRepository<FinanceCategoryEntity, Integer> {

    @Query(value = "select * from finance_category "
            + "where id = (select finance_category_id from ce_code where id = ?1)", nativeQuery = true)
    FinanceCategoryEntity findByCeCode(Integer ceCode);

    FinanceCategoryEntity findFirstByText(String text);

}
