/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.project.internalblock.InternalBlockType;
import uk.gov.london.ops.project.internalblock.InternalProjectBlock;

public interface InternalProjectBlockRepository extends JpaRepository<InternalProjectBlock, Integer> {

    InternalProjectBlock findByProjectIdAndType(Integer projectId, InternalBlockType type);

    @Modifying
    @Query(value = "update internal_project_block set detached_block_project_id = project_id "
            + "where project_id in (?1) and display_order = ?2", nativeQuery = true)
    int copyProjectIdToDetachedID(Integer[] projectIds, Integer displayOrder);

    @Modifying
    @Query(value = "update internal_project_block set project_id = null "
            + "where project_id in (?1) and display_order = ?2", nativeQuery = true)
    int deleteProjectIdFromProjectBlock(Integer[] projectIds, Integer displayOrder);

    int countAllByDetachedProjectIdAndProjectIsNull(Integer id);

    @Modifying
    @Query(value = "update internal_project_block set project_id = detached_block_project_id, detached_block_project_id =null "
            + "where detached_block_project_id in (?1) and display_order = ?2", nativeQuery = true)
    int reattachRemovedInternalBlock(Integer[] projectIds, Integer displayOrder);


}
