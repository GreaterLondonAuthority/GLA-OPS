/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.integration.poc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptAllFileListFilter;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.xml.source.DomSourceFactory;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectDetailsBlock;
import uk.gov.london.ops.domain.template.Programme;
import uk.gov.london.ops.repository.OrganisationRepository;
import uk.gov.london.ops.repository.ProgrammeRepository;
import uk.gov.london.ops.repository.ProjectRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Integration Processor for SAP XML files.
 *
 * Uses Spring Integration components to load XML files, then processes
 * them using XPath expressions to interpret the finance information
 * and link it to projects.
 *
 * Ensure that the Spring profile "sap-poc" is active when testing. For example...
 *
 *     mvn spring-boot:run -Dspring.profiles.active=local,sap-poc
 *
 * @author Steve Leach
 */
@Component
@Profile("sap-poc")
public class FinanceLoader {

    public static final String POC_LOCK = "POC_LOCK";
    Logger log = LoggerFactory.getLogger(getClass());

    private static final File inputDirectory = new File("input");
    private static final File processedDirectory = new File(inputDirectory,"processed");
    private static final File errorDirectory = new File(inputDirectory,"error");

    MessageSource<File> source;
    FtpInboundFileSynchronizingMessageSource ftpSource;
    XPathFactory factory = XPathFactory.newInstance();

    @Resource
    private FinanceServicePOC financeService;

    @Resource
    private ProjectRepository projects;

    @Resource
    private OrganisationRepository organisations;

    @Resource
    private ProgrammeRepository programmes;

    @Autowired
    JdbcLockRegistry lockRegistry;

    @PostConstruct
    public void init() {
        setupTestProjects();
        setupFileSource();
        setupFtpSource();
    }

    private void setupFileSource() {
        processedDirectory.mkdirs();

        FileReadingMessageSource fileSource = new FileReadingMessageSource();
        fileSource.setDirectory(inputDirectory);
        fileSource.setFilter(new AcceptAllFileListFilter<>());

        /*  Using a FileReadingMessageSource here to scan a directory.

            See 15.4.2 in http://docs.spring.io/spring-integration/reference/html/ftp.html
            for an example of creating an FTP MessageSource<File> instead.
         */

        log.info("Integration POC scanning " + inputDirectory.getAbsolutePath());

        fileSource.start();

        this.source = fileSource;
    }

    private void setupFtpSource() {
        DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();

        // Just use a public FTP server from a US university for testing the FTP technology
        factory.setHost("ftp.swfwmd.state.fl.us");
        factory.setPort(21);
        factory.setUsername("anonymous");
        factory.setPassword("");

        FtpInboundFileSynchronizer fileSynchronizer = new FtpInboundFileSynchronizer(factory);
        fileSynchronizer.setDeleteRemoteFiles(false);
        fileSynchronizer.setRemoteDirectory("/pub/amr");
        fileSynchronizer.setFilter(new FtpSimplePatternFileListFilter("*.dat"));

        ftpSource = new FtpInboundFileSynchronizingMessageSource(fileSynchronizer);

        ftpSource.setLocalDirectory(new File("input/ftpdir"));
        ftpSource.setAutoCreateLocalDirectory(true);
        ftpSource.setLocalFilter(new AcceptOnceFileListFilter<File>());
        try {
            ftpSource.afterPropertiesSet();
        } catch (Exception e) {
            //log.error("Error initialising FTP source", e);
            ftpSource = null;
            return;
        }

        ftpSource.start();
    }

    private void setupTestProjects() {
        addProject("Finance Test Project 1", 500089);
        addProject("Finance Test Project 2", 500114);
    }

    private void addProject(String title, Integer sapNumber) {
        Programme prog = programmes.findAll().get(0);

        Project project = new Project();
        project.addBlockToProject(new ProjectDetailsBlock(project));
        project.setTitle(title);
        project.getDetailsBlock().setLegacyProjectCode(sapNumber);
        project.setOrganisation(organisations.findOne(9999));
        project.setProgramme(prog);
        project.setTemplate(prog.getTemplates().iterator().next());

        projects.save(project);
    }

    @Scheduled(fixedDelay = 2000)
    public void processInputFiles() {
        log.debug("Integration POC waking up...");

        Lock lock = lockRegistry.obtain(POC_LOCK);
        if (lock != null) {
            lock.lock();
            log.debug("  Got lock");

            Message<File> message = source.receive();

            while (message != null) {
                processMessage(message);

                message = source.receive();
            }

            lock.unlock();
            log.debug("  Released lock");
        }

        log.debug("Integration POC sleeping.");
    }

    @Scheduled(fixedDelay = 20000)
    public void ftpTest() throws Exception {
        log.debug("Integration POC checking FTP...");

        Message<File> message = ftpSource.receive();

        while (message != null) {
            log.debug("Processing FTP file: " + message.getPayload().getName());
            processMessage(message);
            message = ftpSource.receive();
        }

        log.debug("Integration POC FTP poller sleeping.");
    }

    private void processMessage(Message<File> message) {
        File inputFile = message.getPayload();

        if (!inputFile.isFile()) {
            return;
        }

        if (isOrdersFile(inputFile)) {
            loadOrdersFile(inputFile);
        } else if (isPaymentsFile(inputFile)) {
            loadPaymentsFile(inputFile);
        } else {
            log.warn("Unknown input file type: {}", inputFile.getName());
            moveFile(inputFile, errorDirectory);
        }
    }

    private void moveFile(File inputFile, File target) {
        errorDirectory.mkdirs();
        String name = inputFile.getName();
        File processedFile = new File(target,name);
        processedFile.delete();
        inputFile.renameTo(processedFile);
        log.info("Moved to " + processedFile);
    }

    private void loadPaymentsFile(File inputFile) {
        log.info("Loading payments file {}", inputFile.getName());

        Node root = loadXmlFile(inputFile);

        loadPaymentsFile(root);

        moveFile(inputFile,processedDirectory);
    }

    private void loadPaymentsFile(Node root) {
        XPath xpath = factory.newXPath();

        List<FinancePaymentLine> lines = new LinkedList<>();

        try {
            NodeList nodes = (NodeList)xpath.evaluate("//data",root, XPathConstants.NODESET);
            for (int n = 0; n < nodes.getLength(); n++) {
                Node data = nodes.item(n);

                FinancePaymentLine line = getFinancePaymentLine(data);

                if (line != null) {
                    lines.add(line);
                }
            }
        } catch (XPathExpressionException e) {
            log.error("Error processing message", e);
        }

        if (lines.size() > 0) {
            financeService.createPaymentLines(lines);
        }
    }

    private FinancePaymentLine getFinancePaymentLine(Node data) throws XPathExpressionException {
        XPath xpath = factory.newXPath();

        String projectNumber = (String) xpath.evaluate("./PCSProjectNumber", data, XPathConstants.STRING);

        Project project = findProjectBySAPNumber(projectNumber);

        if (project == null) {
            log.warn("Could not find project with PCS number " + projectNumber);
            return null;
        }

        FinancePaymentLine line = new FinancePaymentLine();
        line.setProject(project);
        line.setReference((String)xpath.evaluate("./paymentReference",data,XPathConstants.STRING));
        line.setAmount(new BigDecimal((String)xpath.evaluate("./paidAmount",data,XPathConstants.STRING)));
        line.setProjectNumber(projectNumber);

        return line;
    }

    private boolean isPaymentsFile(File file) {
        return file.getName().startsWith("Payments_");
    }

    private boolean isOrdersFile(File file) {
        return file.getName().startsWith("Orders_");
    }

    private void loadOrdersFile(File inputFile) {
        log.info("Loading orders file {}", inputFile.getName());

        Node root = loadXmlFile(inputFile);

        loadOrdersFile(root);

        moveFile(inputFile,processedDirectory);
    }

    private Node loadXmlFile(File inputFile) {
        DOMSource source = (DOMSource) (new DomSourceFactory().createSource(inputFile));
        return source.getNode();
    }

    private void loadOrdersFile(Node root) {
        XPath xpath = factory.newXPath();

        List<FinanceOrderLine> lines = new LinkedList<>();

        try {
            NodeList nodes = (NodeList)xpath.evaluate("//data",root, XPathConstants.NODESET);
            for (int n = 0; n < nodes.getLength(); n++) {
                Node data = nodes.item(n);

                FinanceOrderLine orderLine = getFinanceOrderLine(data);

                if (orderLine != null) {
                    lines.add(orderLine);
                }
            }
        } catch (XPathExpressionException e) {
            log.error("Error processing message", e);
        }

        if (lines.size() > 0) {
            financeService.createOrderLines(lines);
        }
    }

    private FinanceOrderLine getFinanceOrderLine(Node data) throws XPathExpressionException {
        XPath xpath = factory.newXPath();

        String projectNumber = (String) xpath.evaluate("./PCSProjectNumber", data, XPathConstants.STRING);

        Project project = findProjectBySAPNumber(projectNumber);

        if (project == null) {
            log.warn("Could not find project with PCS number " + projectNumber);
            return null;
        }

        FinanceOrderLine orderLine = new FinanceOrderLine();
        orderLine.setProject(project);
        orderLine.setOrderNumber((String)xpath.evaluate("./orderNumber",data,XPathConstants.STRING));
        orderLine.setOrderLineNumber((String)xpath.evaluate("./orderLineNumber",data,XPathConstants.STRING));
        orderLine.setAmount(new BigDecimal((String)xpath.evaluate("./orderLineAmount",data,XPathConstants.STRING)));
        orderLine.setProjectNumber(projectNumber);

        return orderLine;
    }

    private Project findProjectBySAPNumber(String projectNumber) {
        for (Project project : projects.findAll()) {
            if ((project.getLegacyProjectCode() != null) && (project.getLegacyProjectCode().equals(projectNumber))) {
                return project;
            }
        }
        return null;
    }

    @PreDestroy
    public void shutDown() {
        ((FileReadingMessageSource)source).stop();
        ftpSource.stop();
        log.info("Integration POC shut down");
    }

}
