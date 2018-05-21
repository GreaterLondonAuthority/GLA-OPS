/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.london.ops.domain.project.LockDetails;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Created by chris on 10/01/2017.
 */
@Repository
public interface LockDetailsRepository extends JpaRepository<LockDetails, Integer>  {

    @Transactional
    @Modifying
    @Query("delete from uk.gov.london.ops.domain.project.LockDetails ld where ld.lockTimeoutTime  < ?1")
    int deleteAllByLockTimeoutTimeBefore(OffsetDateTime time);

    @Transactional
    @Modifying
    int deleteAllByProjectId(Integer projectId);


    List<LockDetails> findAllByLockTimeoutTimeBefore(OffsetDateTime time);
}
