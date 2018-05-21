/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.importdata.ImportErrorLog;
import uk.gov.london.ops.domain.importdata.ImportJobStatus;
import uk.gov.london.ops.domain.importdata.ImportJobType;
import uk.gov.london.ops.domain.importdata.ImportProcessLog;
import uk.gov.london.ops.repository.ImportErrorLogRepository;
import uk.gov.london.ops.repository.ImportProcessLogRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(Transactional.TxType.REQUIRES_NEW)
public class ImportLogService {

    @Autowired
    private UserService userService;

    @Autowired
    private ImportProcessLogRepository importProcessLogRepository;

    @Autowired
    private ImportErrorLogRepository importErrorLogRepository;

    @Autowired
    private Environment environment;

    public List<ImportErrorLog> findAllErrorsByImportType(ImportJobType type) {
        return importErrorLogRepository.findAllByImportJobType(type);
    }

    public void deleteAllErrorsByImportType(ImportJobType type) {
         importErrorLogRepository.deleteAllByImportJobType(type);
    }

    public ImportProcessLog recordImport(ImportJobType type) {
        return importProcessLogRepository.save(new ImportProcessLog(type, environment.now()));
    }

    public ImportProcessLog recordImport(ImportJobType type, ImportJobStatus status) {
        return importProcessLogRepository.save(new ImportProcessLog(type, environment.now(), status));
    }

    public void updateImportLog(ImportProcessLog importProcessLog, ImportJobStatus status, int importCount, int errorCount) {
        importProcessLog.setImportJobStatus(status);
        importProcessLog.setImportCount(importCount);
        importProcessLog.setErrorCount(errorCount);
        importProcessLogRepository.save(importProcessLog);
    }

    public void recordError(ImportJobType type, String message, int rowIndex, String rowSource) {
        importErrorLogRepository.save(new ImportErrorLog(type, environment.now(), userService.currentUser().getUsername(), rowIndex, message, rowSource));
    }

    public void recordErrors(Iterable<ImportErrorLog> errors) {
        importErrorLogRepository.save(errors);
    }

}
