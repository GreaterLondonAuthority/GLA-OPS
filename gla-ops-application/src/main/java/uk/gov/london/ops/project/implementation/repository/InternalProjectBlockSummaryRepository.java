/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.project.internalblock.InternalProjectBlockSummary;

import java.util.Set;

public interface InternalProjectBlockSummaryRepository extends JpaRepository<InternalProjectBlockSummary, Integer> {

    Set<InternalProjectBlockSummary> findAllByProjectIdAndShow(Integer projectId, boolean show);

}
