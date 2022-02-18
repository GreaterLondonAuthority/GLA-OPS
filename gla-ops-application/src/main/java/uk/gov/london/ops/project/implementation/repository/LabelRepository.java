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
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.project.label.Label;

import java.util.Set;

@Repository
public interface LabelRepository extends JpaRepository<Label, Integer>  {

    @Query(value = "select l.* from Label l inner join project_block_label pbl "
            + "on pbl.project_block_id = ?1 and pbl.label_id = l.id", nativeQuery = true)
    Set<Label> findLabelsForBlock(Integer blockId);

    Set<Label> getAllByProjectId(Integer projectId);

    void deleteByProjectId(Integer projectId);

}
