/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.london.ops.domain.importdata.ImportErrorLog;
import uk.gov.london.ops.domain.importdata.ImportJobType;

import java.util.List;

public interface ImportErrorLogRepository extends JpaRepository<ImportErrorLog, Integer> {

    List<ImportErrorLog> findAllByImportJobType(ImportJobType type);

    void deleteAllByImportJobType(ImportJobType type);
}
