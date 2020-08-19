/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.config;

import com.jcraft.jsch.ChannelSftp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import uk.gov.london.ops.payment.implementation.sap.MoveItSynchroniser;

import java.io.File;
import java.util.List;

/**
 * SFTP Connection to sync directories
 * Created by chris on 25/01/2017.
 */
@Configuration
//@ConditionalOnExpression(value="false")
@EnableIntegration
public class SpringSFTPConfiguration {

    public static final String SYNC_ACTUALS_TASK_KEY = "MOVEIT_SYNC";
    public static final String SYNC_INVOICES_TASK_KEY = "SYNC_INVOICES";

    private static final String SYNC_ACTUALS_LOCK_KEY = "SFTP_CLUSTER_LOCK";
    private static final String SYNC_INVOICES_LOCK_KEY = "SYNC_INVOICES_LOCK";

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcLockRegistry lockRegistry;

    @Value("${sap.ftp.inbound.path}")
    private String ftpInboundDir;

    @Value("${sap.ftp.inbound.path.invoices}")
    private String ftpInboundInvoicesDir;

    @Value("${sap.ftp.remote.path.outgoing}")
    private String ftpRemoteOutgoingDir;

    @Value("${sap.ftp.remote.path.outgoing.invoices}")
    private String ftpRemoteOutgoingInvoicesDir;

    @Value("${sap.ftp.remote.path.incoming}")
    private String ftpRemoteIncomingDir;

    @Value("${sap.ftp.username}")
    private String username;

    @Value("${sap.ftp.password}")
    private String password;

    @Value("${sap.ftp.hostname}")
    private String hostname;

    @Value("${sap.ftp.enabled}")
    private boolean enabled;

    @Value("${sap.ftp.port}")
    private Integer port;

    @Value("${sap.ftp.timeout}")
    private Integer timeout;

    @Value("${sap.ftp.delete.remote.files}")
    private boolean deleteRemoteFiles;

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        CachingSessionFactory<ChannelSftp.LsEntry> lsEntryCachingSessionFactory = null;
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(hostname);
        factory.setPort(port);
        factory.setUser(username);
        factory.setPassword(password);
        factory.setTimeout(timeout);
        factory.setAllowUnknownKeys(true);
        lsEntryCachingSessionFactory = new CachingSessionFactory<>(factory);

        return lsEntryCachingSessionFactory;
    }

    @Bean(name = "actualsSynchroniser")
    public MoveItSynchroniser actualsSynchroniser() {
        MoveItSynchroniser fileSynchroniser = new MoveItSynchroniser(sftpSessionFactory(), lockRegistry, SYNC_ACTUALS_TASK_KEY, SYNC_ACTUALS_LOCK_KEY);
        fileSynchroniser.setDeleteRemoteFiles(deleteRemoteFiles);
        fileSynchroniser.setRemoteDirectory(ftpRemoteOutgoingDir);
        fileSynchroniser.setLocalDirectory(ftpInboundDir);
        fileSynchroniser.setPaused(!enabled);
        return fileSynchroniser;
    }

    @Bean(name = "invoiceResponseSynchroniser")
    public MoveItSynchroniser invoiceResponseSynchroniser() {
        MoveItSynchroniser fileSynchronizer = new MoveItSynchroniser(sftpSessionFactory(), lockRegistry, SYNC_INVOICES_TASK_KEY, SYNC_INVOICES_LOCK_KEY);
        fileSynchronizer.setDeleteRemoteFiles(deleteRemoteFiles);
        fileSynchronizer.setRemoteDirectory(ftpRemoteOutgoingInvoicesDir);
        fileSynchronizer.setLocalDirectory(ftpInboundInvoicesDir);
        fileSynchronizer.setPaused(!enabled);
        return fileSynchronizer;
    }

    private void testSftpConnection(MoveItSynchroniser fileSynchronizer) {
        log.info("Testing SFTP file sync...");
        List<String> remoteFileList = fileSynchronizer.getRemoteFileList();
        for (String fileName : remoteFileList) {
            log.info("  Found " + fileName);
        }
    }

    @Bean(name = "MessageSource")
    @InboundChannelAdapter(channel = "sftpChannel", poller = @Poller(fixedRate = "300000", maxMessagesPerPoll = "-1") )
    public MessageSource<File> sftpMessageSource() {
        SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(actualsSynchroniser());
        source.setLocalDirectory(new File(ftpInboundDir));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<>());
        log.info("sftpMessageSource created");
        return source;
    }

    @Bean(name = "InvoiceResponsesMessageSource")
    @InboundChannelAdapter(channel = "invoiceResponsesSftpChannel", poller = @Poller(fixedRate = "60000", maxMessagesPerPoll = "-1") )
    public MessageSource<File> invoiceResponsesMessageSource() {
        SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(invoiceResponseSynchroniser());
        source.setLocalDirectory(new File(ftpInboundInvoicesDir));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<>());
        return source;
    }

}
