/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap;

import com.jcraft.jsch.ChannelSftp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTask;
import uk.gov.london.ops.framework.scheduledtask.ScheduledTaskService;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Bespoke SftpInboundFileSynchroniser with additional logging and control features.
 *
 * Includes a default filter for XML files only.
 *
 * @author Steve Leach
 */
public class MoveItSynchroniser extends SftpInboundFileSynchronizer {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private final JdbcLockRegistry lockRegistry;

    private final SessionFactory<ChannelSftp.LsEntry> sessionFactory;

    private final String scheduledTaskName;
    private final String lockName;

    private boolean paused = false;
    private String remoteDirectory = null;
    private String localDirectory = null;
    private int syncCount = 0;

    @Autowired
    public ScheduledTaskService scheduledTaskService;

    @Autowired
    private Environment environment;

    @Value("${sap.ftp.error-logging-interval}")
    private final Integer errorLoggingInterval = 30;

    public MoveItSynchroniser(SessionFactory<ChannelSftp.LsEntry> sessionFactory, JdbcLockRegistry lockRegistry,
                              String scheduledTaskName, String lockName) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
        this.lockRegistry = lockRegistry;
        this.scheduledTaskName = scheduledTaskName;
        this.lockName = lockName;
        super.setFilter(new SftpSimplePatternFileListFilter("*.xml"));
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        log.info("Setting paused to " + paused);
    }

    public int localFileCount() {
        File[] files = new File(localDirectory).listFiles();
        return files != null ? files.length : -1;
    }

    @Override
    public void synchronizeToLocalDirectory(File localDirectory) {
        synchronizeToLocalDirectory(localDirectory, Integer.MIN_VALUE);
    }

    @Override
    public void synchronizeToLocalDirectory(File localDirectory, int maxFetchSize) {
        if (paused) {
            scheduledTaskService.update(scheduledTaskName, ScheduledTask.SKIPPED, "MoveIT sync is paused");
        } else {
            Lock lock = lockRegistry.obtain(lockName);
            try {
                if (lock != null && lock.tryLock()) {
                    log.debug("Synchronising...");
                    super.synchronizeToLocalDirectory(localDirectory, maxFetchSize);
                    syncCount++;
                    scheduledTaskService.update(scheduledTaskName, ScheduledTask.SUCCESS, "Local files = " + localFileCount());
                    log.debug("Synchronisation complete");
                } else {
                    scheduledTaskService.update(scheduledTaskName, ScheduledTask.ERROR, "Could not get lock");
                }
            } catch (Exception e) {
                scheduledTaskService.update(scheduledTaskName, e);
                logError("Error synchronising MoveIT files");
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }
    }

    void logError(String message) {
        ScheduledTask scheduledTask = scheduledTaskService.findOne(scheduledTaskName);
        if (scheduledTask != null && scheduledTask.getLastSuccess() != null
                && ChronoUnit.MINUTES.between(scheduledTask.getLastSuccess(), environment.now()) >= errorLoggingInterval) {
            log.error(message);
        }  else {
            log.info(message);
        }
    }

    @Override
    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
        super.setRemoteDirectory(remoteDirectory);
    }

    public String getRemoteDirectory() {
        return this.remoteDirectory;
    }

    /**
     * Returns the number of times the synchronisation has occurred.
     */
    public int getSyncCount() {
        return syncCount;
    }

    public List<String> getRemoteFileList() {
        if (remoteDirectory == null) {
            throw new IllegalStateException("Remote directory not specified");
        }

        List<String> remoteFiles = new LinkedList<>();
        long start = System.currentTimeMillis();
        Session<ChannelSftp.LsEntry> session = null;
        try {
            session = sessionFactory.getSession();
            ChannelSftp.LsEntry[] files = session.list(remoteDirectory);
            for (ChannelSftp.LsEntry entry : files) {
                remoteFiles.add(entry.getFilename());
            }
            if (files.length == 0) {
                log.info(String.format("No files found in : %s ", remoteDirectory));
            }
            log.info("Connection took: " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            log.error("Failed to connect after:  "  + (System.currentTimeMillis() - start) + "ms : " + e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return remoteFiles;
    }

    /**
     * Initiates an immediate sync, as long as the synchronizer is not paused.
     */
    public void sync() {
        this.synchronizeToLocalDirectory(new File(localDirectory));
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getLocalDirectory() {
        return this.localDirectory;
    }
}
