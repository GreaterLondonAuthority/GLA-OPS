/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.MapResult;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTask;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTaskService;
import uk.gov.london.ops.payment.implementation.ProjectLedgerEntryMapper;
import uk.gov.london.ops.payment.implementation.repository.SapDataRepository;
import uk.gov.london.ops.user.UserServiceImpl;
import uk.gov.london.ops.user.domain.UserEntity;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

@Service
public class SapDataService {

    Logger log = LoggerFactory.getLogger(getClass());

    static final String SAP_DATA_LOCK = "SAP_DATA_LOCK";
    static final String TASK_KEY = "SAP_DATA";

    @Autowired
    FinanceService financeService;

    @Autowired
    ScheduledTaskService scheduledTaskService;

    @Autowired
    ProjectLedgerEntryMapper projectLedgerEntryMapper;

    @Autowired
    SapDataRepository sapDataRepository;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Autowired
    Environment environment;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    AuditService auditService;

    public List<SapData> getSapData(Boolean processed) {
        if (processed != null) {
            ArrayList<SapData> resp = new ArrayList<>();
            List<SapData> list = sapDataRepository.findAllByProcessed(processed);
            for (SapData element : list) {
                final String interfaceType = element.getInterfaceType();
                if (interfaceType.equals(SapData.TYPE_PAYMENT) || interfaceType.equals(SapData.TYPE_RECEIPT)) {
                    try {
                        element.setModel(projectLedgerEntryMapper.getModel(element, false));
                    } catch (RuntimeException e) {
                        log.warn("Error parsing XML for SAP data item " + element.getId());
                    }
                    resp.add(element);
                }
            }
            return resp;
        } else {
            return sapDataRepository.findAll();
        }
    }

    /**
     * Turns the downloaded SAP data into project ledger entries.
     */
    @Scheduled(initialDelay = 60000, fixedDelay = 300000)
    @Transactional
    public void processSapData() throws IOException {
        int processed = 0;
        int errors = 0;
        int ignored = 0;

        long start = System.currentTimeMillis();

        Lock lock = lockRegistry.obtain(SAP_DATA_LOCK);
        try {
            if (lock != null && lock.tryLock()) {
                List<SapData> sapDataList = sapDataRepository.findAllByProcessed(false);
                for (SapData sapData : sapDataList) {
                    if (isOrdersFile(sapData)) {
                        sapData.setProcessed(true);
                        sapData.setErrorDescription("Ignored orders file");
                        ignored++;
                    } else if (sapData.getInterfaceType().equals(SapData.TYPE_INV_RESP)) {
                        // TODO: implement this
                        // See InvoiceResponseMessageEndpointTest.java
                    } else {
                        try {
                            MapResult<ProjectLedgerEntry> mapResult = projectLedgerEntryMapper.map(sapData);

                            if (mapResult.getDataObject() != null) {
                                // Can create a ledger entry for the item
                                financeService.save(mapResult.getDataObject());
                                sapData.setProcessed(true);
                                sapData.setErrorDescription(null);
                                processed++;
                            } else if (mapResult.isIgnored()) {
                                // Item should be ignored
                                sapData.setProcessed(true);
                                sapData.setErrorDescription(mapResult.getError());
                                ignored++;
                            } else {
                                // Ledger entry could not be created
                                sapData.setProcessed(false);
                                sapData.setErrorDescription(mapResult.getError());
                                errors++;
                            }
                        } catch (Exception e) {
                            if (newOrDifferentError(e, sapData)) {
                                // Only log the error if it is a new one
                                log.error("Error processing SAP data row " + sapData.getId(), e);
                            }
                            sapData.setErrorDescription(e.getMessage());
                            errors++;
                        }
                    }
                    sapData.setProcessedOn(environment.now());
                    sapDataRepository.save(sapData);
                }

                int elapsed = (int) (System.currentTimeMillis() - start);
                String taskSummary = String.format("%d processed, %d errors, %d ignored (%d ms)",
                        processed, errors, ignored, elapsed);
                scheduledTaskService.update(TASK_KEY, ScheduledTask.SUCCESS, taskSummary);
                if (processed > 0) {
                    log.info("SAP data process: " + taskSummary);
                } else {
                    log.debug("SAP data process: " + taskSummary);
                }
            } else {
                scheduledTaskService.update(TASK_KEY, ScheduledTask.SKIPPED, "Could not obtain lock: " + SAP_DATA_LOCK);
                log.debug("Unable to obtain lock: " + SAP_DATA_LOCK);
            }

        } catch (Exception e) {
            scheduledTaskService.update(TASK_KEY, e);
            log.error("Error processing sap_data", e);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private boolean isOrdersFile(SapData sapData) {
        return (sapData != null) && (sapData.getFileName() != null) && sapData.getFileName().startsWith("Orders_");
    }

    /**
     * Returns true if this is a new error or a different one from previously.
     */
    private boolean newOrDifferentError(Exception e, SapData sapData) {
        return (sapData == null)
                || (sapData.getErrorDescription() == null)
                || (!Objects.equals(e.getMessage(), sapData.getErrorDescription()));
    }

    @Transactional
    public SapData save(SapData sapData) {
        return sapDataRepository.save(sapData);
    }

    public long countByFileName(String fileName) {
        return sapDataRepository.countByFileName(fileName);
    }

    public long totalSapDataEntries() {
        return sapDataRepository.count();
    }

    public long errorCount() {
        int errors = 0;
        List<SapData> unprocessed = sapDataRepository.findAllByProcessed(false);
        for (SapData sapData : unprocessed) {
            if (sapData.getErrorDescription() != null) {
                errors++;
            }
        }
        return errors;
    }

    public SapData markSapDataIgnored(Integer sapDataId, Boolean ignore) {

        UserEntity user = userService.currentUser();
        SapData sapData = sapDataRepository.findById(sapDataId).orElse(null);

        if (sapData == null) {
            throw new ValidationException("Unable to find record with specified ID");
        }

        if (ignore) {
            sapData.setAction(SapData.Action.IGNORED);
            sapData.setActionedByUsername(user.getUsername());
            sapData.setProcessed(true);
            String fileName = sapData.getFileName();
            if (fileName.length() > 99) {
                fileName = fileName.substring(0, 99);
            }
            auditService.auditCurrentUserActivity(String.format("%s SAP file is ignored", fileName));
        } else {
            if (SapData.Action.IGNORED.equals(sapData.getAction())) {
                sapData.setAction(null);
                sapData.setActionedByUsername(null);
                sapData.setProcessed(false);
            } else {
                throw new ValidationException("Sap data record is not currently ignored. ");
            }
        }
        return sapDataRepository.save(sapData);
    }

}
