/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.project.block.NamedProjectBlock;

@Transactional
public interface ProjectBlockRepository extends JpaRepository<NamedProjectBlock, Integer> {

    @Modifying
    @Query(value = "update project_block set detached_block_project_id = project_id " +
            "where project_id in (?1) and display_order = ?2", nativeQuery = true)
    int copyProjectIdToDetachedID(Integer[] projectIds, Integer displayOrder);

    @Modifying
    @Query(value = "update project_block set project_id = null, latest_for_project = null " +
            "where project_id in (?1) and display_order = ?2", nativeQuery = true)
    int deleteProjectIdFromProjectBlock(Integer[] projectIds, Integer displayOrder);

    @Modifying
    @Query(value = "update project_block set project_id = detached_block_project_id, detached_block_project_id =null " +
            "where detached_block_project_id in (?1) and display_order = ?2", nativeQuery = true)
    int reattachRemovedBlock(Integer[] projectIds, Integer displayOrder);

    @Modifying
    @Query(value = "update project_block set latest_for_project = project_id " +
            "where project_id in (?1) and display_order = ?2 and latest_version = true", nativeQuery = true)
    int reattachLatestBlock(Integer[] projectIds, Integer displayOrder);

    int countAllByDetachedProjectIdAndProjectIsNull(Integer id);


    @Modifying
    @Query(value = "update project_details_block set " +
            "address_restricted  = true, " +
            "address  = 'Restricted', " +
            "ward_id  = null, " +
            "postcode  = null, " +
            "coord_x  = null, " +
            "coord_y  = null, " +
            "planning_permission_reference  = null " +
            "where id in (select id from project_block where project_id = ?1 and BLOCK_TYPE ='DETAILS')", nativeQuery = true)
    void deleteRestrictedData(Integer projectId);

    @Modifying
    @Query(value = "update project_details_block set " +
            "address_restricted  = false, " +
            "address  = null " +
            "where id in (select id from project_block where project_id = ?1 and BLOCK_TYPE ='DETAILS')", nativeQuery = true)
    void resetRestrictedFlag(Integer projectId);

    @Query(value = "select * from project_block pb inner join learning_grant_block lgb on lgb.id = pb.id where project_id = ?1 and version_number = ?2 and display_order = ?3", nativeQuery = true)
    NamedProjectBlock findLearningGrantBlockByProjectIdAndVersionNumberAndDisplayOrder(Integer projectId, Integer versionNumber, Integer displayOrder);
}
