/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.project.outputs.OutputCategoryAssumption;

import java.util.Set;

public interface OutputCategoryAssumptionRepository extends JpaRepository<OutputCategoryAssumption, Integer> {

    Set<OutputCategoryAssumption> findAllByBlockIdAndYear(Integer blockId, Integer year);

    Set<OutputCategoryAssumption> findAllByBlockId(Integer originalBlockId);

    Set<OutputCategoryAssumption> findAllByProjectId(Integer projectId);

    @Query(value = "select oca.* from Output_Category_Assumption oca "
            + "inner join project p on oca.project_id = p.id "
            + "inner join template t on p.template_id = t.id and t.id = ?1 "
            + "where oca.category = ?2", nativeQuery = true)
    Set<OutputCategoryAssumption> findAllAffectedByNameChange(Integer templateId, String oldCategoryName);
}
