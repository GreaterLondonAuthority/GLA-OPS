/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.payment.SapData;

import java.util.List;

public interface SapDataRepository extends JpaRepository<SapData, Integer> {

    List<SapData> findAllByProcessed(boolean processed);

    Long countByFileName(String fileName);

}
