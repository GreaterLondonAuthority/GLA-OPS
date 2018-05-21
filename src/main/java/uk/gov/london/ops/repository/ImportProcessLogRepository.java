/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.domain.importdata.ImportJobType;
import uk.gov.london.ops.domain.importdata.ImportProcessLog;

public interface ImportProcessLogRepository extends JpaRepository<ImportProcessLog, Integer> {

    @Query("select count(pl) > 0 from ImportProcessLog pl where pl.importJobType = ?1")
    boolean existsByImportJobType(ImportJobType jobType);

}
