/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.integration.poc;

import com.jcraft.jsch.ChannelSftp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.integration.xml.source.DomSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.repository.ProjectRepository;
import uk.gov.london.ops.service.finance.SalesInvoiceDocument;

import javax.annotation.Resource;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Stub for a finance information service.
 *
 * @author Steve Leach
 */
@Component
public class FinanceServicePOC {

    Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private ProjectRepository projects;

    XPathFactory factory = XPathFactory.newInstance();

    DefaultSftpSessionFactory ftpSessionFactory = new DefaultSftpSessionFactory(true);
    SftpInboundFileSynchronizingMessageSource sftpSource;

    @Value("${sap.ftp.inbound.path}")
    public String ftpInboundDir;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private String ftpSourcePath = null;

    public void listFilesDirect(PrintWriter output) {
        SftpSession session = null;
        try {
            long start = System.currentTimeMillis();
            session = ftpSessionFactory.getSession();
            ChannelSftp.LsEntry[] files = session.list(ftpSourcePath);
            long elapsed = System.currentTimeMillis() - start;
            output.println("Remote listing complete in " + start + "ms");
            for (ChannelSftp.LsEntry file : files) {
                output.println(file.getLongname());
            }
        } catch (Exception e) {
            outputError(output, "List Files", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void outputError(PrintWriter output, String summary, Exception e) {
        output.println(String.format("Error (%s)...", summary));
        output.println(e.getMessage());
        e.printStackTrace(output);
    }

    public void syncAndProcessFiles(PrintWriter output) {
        try {
            SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(ftpSessionFactory);
            fileSynchronizer.setDeleteRemoteFiles(false);
            fileSynchronizer.setRemoteDirectory(ftpSourcePath);
            fileSynchronizer.setFilter(new SftpSimplePatternFileListFilter("*.xml"));

            sftpSource = new SftpInboundFileSynchronizingMessageSource(fileSynchronizer);

            sftpSource.setLocalDirectory(new File(ftpInboundDir));
            sftpSource.setAutoCreateLocalDirectory(true);
            sftpSource.setLocalFilter(new AcceptOnceFileListFilter<File>());
            try {
                sftpSource.afterPropertiesSet();
            } catch (Exception e) {
                outputError(output, "Set Properties", e);
            }

            sftpSource.start();

            Message<File> message = sftpSource.receive();
            while (message != null) {
                try {
//                    processFile(message.getPayload());

                    output.println("Received file: " + message.getPayload().getName());
                } catch (Exception e) {
                    outputError(output, "Processing file", e);
                }

                message = sftpSource.receive();
            }

            sftpSource.stop();
        } catch (Exception e) {
            outputError(output, "Syncing and processing files", e);
        }

    }

    public void createOrderLines(List<FinanceOrderLine> lines) {
        printOrderHeader();
        for (FinanceOrderLine line : lines) {
            printOrderLine(line);
        }
    }

    private void printOrderHeader() {
        log.info(String.format("%6s %-30s %10s %5s %10s",
                "Prj.ID",
                "Project Title",
                "Order",
                "Line",
                "Amount"
        ));
    }

    private void printOrderLine(FinanceOrderLine orderLine) {
        log.info(String.format("%6d %-30s %10s %5s %10.2f",
                orderLine.getProject().getId(),
                orderLine.getProject().getTitle(),
                orderLine.getOrderNumber(),
                orderLine.getOrderLineNumber(),
                orderLine.getAmount()
        ));
    }

    public void createPaymentLines(List<FinancePaymentLine> lines) {
        // printPaymentsHeader();
        for (FinancePaymentLine line : lines) {
            createTestSAPPayment(line);
            // printPaymentLine(line);
        }
    }

    private void createTestSAPPayment(FinancePaymentLine line) {
        if (line.getAmount() == null) {
            line.setAmount(new BigDecimal(0));
        }
        long projectId = line.getProject() == null ? 0L : line.getProject().getId();
        jdbcTemplate.update("insert into sap_payment (pcs_project_code, project_id, order_line_amount, order_number) " +
                "values (?, ?, ?, ?)", line.getProjectNumber(), projectId, line.getAmount(), line.getReference());
    }

    private void printPaymentLine(FinancePaymentLine line) {
        log.info(String.format("%6d %-30s %10s %10.2f",
                line.getProject() == null ? 0 : line.getProject().getId(),
                line.getProject() == null ? "" : line.getProject().getTitle(),
                line.getReference(),
                line.getAmount()
        ));
    }

    private void printPaymentsHeader() {
        log.info(String.format("%6s %-30s %10s %10s",
                "Prj.ID",
                "Project Title",
                "Reference",
                "Amount"
        ));
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
            createPaymentLines(lines);
        }
    }

    private FinancePaymentLine getFinancePaymentLine(Node data) throws XPathExpressionException {
        XPath xpath = factory.newXPath();

        String projectNumber = (String) xpath.evaluate("./PCSProjectNumber", data, XPathConstants.STRING);

        Project project = findProjectBySAPNumber(projectNumber);

        if (project == null) {
            log.warn("Could not find project with PCS number " + projectNumber);
        }

        FinancePaymentLine line = new FinancePaymentLine();
        line.setProject(project);
        line.setReference((String)xpath.evaluate("./paymentReference",data,XPathConstants.STRING));
        line.setAmount(getMonetaryValue(data, xpath));
        line.setProjectNumber(projectNumber);

        return line;
    }

    private BigDecimal getMonetaryValue(Node data, XPath xpath) throws XPathExpressionException {
        String textValue = (String) xpath.evaluate("./paidAmount", data, XPathConstants.STRING);;
        try {
            return new BigDecimal(textValue);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse numeric value: " + textValue);
            return new BigDecimal(0);
        }
    }

    private Project findProjectBySAPNumber(String projectNumber) {
        for (Project project : projects.findAll()) {
            if ((project.getLegacyProjectCode() != null) && (project.getLegacyProjectCode().equals(projectNumber))) {
                return project;
            }
        }
        return null;
    }

    private boolean isPaymentsFile(File file) {
        return file.getName().startsWith("Payments_");
    }

    private Node loadXmlFile(File inputFile) {
        DOMSource source = (DOMSource) (new DomSourceFactory().createSource(inputFile));
        return source.getNode();
    }

    public void processFile(File file) {
        if (isPaymentsFile(file)) {
            Node root = loadXmlFile(file);
            loadPaymentsFile(root);
        }
    }

    public void configureFTP(String host, Integer port, String path, String user, String password, Integer timeout) {
        this.ftpSourcePath = path;
        ftpSessionFactory.setHost(host);
        ftpSessionFactory.setUser(user);
        ftpSessionFactory.setPassword(password);
        ftpSessionFactory.setPort(port);
        ftpSessionFactory.setAllowUnknownKeys(true);
        ftpSessionFactory.setTimeout(timeout);
    }

    public String createInvoiceXmlString(SalesInvoiceDocument invoice) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("uk.gov.london.ops.service.finance");
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        marshaller.setMarshallerProperties(props);
        StringResult result = new StringResult();

        marshaller.marshal(invoice, result);

        return result.toString();
    }

    public void sendSapTestFile(PrintWriter output) {
        SftpSession session = null;
        try {
            session = ftpSessionFactory.getSession();
            session.getClientInstance().start();

            SalesInvoiceDocument invoice = createSampleInvoice();
            invoice.invoiceReferences.SuppliersInvoiceNumber = "" + new Date().getTime();
            String content = createInvoiceXmlString(invoice);

            String fileName = String.format("%s/invoice-%s.xml",this.ftpSourcePath, invoice.invoiceReferences.SuppliersInvoiceNumber );

            InputStream data = new ByteArrayInputStream(content.getBytes());

            session.write(data, fileName);

            data.close();

            output.println("File written: " + fileName);
        } catch (Exception e) {
            outputError(output, "Write test file", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public SalesInvoiceDocument createSampleInvoice() {
        SalesInvoiceDocument invoice = new SalesInvoiceDocument();
        invoice.narrative = "Test by ";
//        invoice.amount = new BigDecimal(1234.56);
//        invoice.addLine("9876.54");

        return invoice;
    }

    public void listLocalFiles(String folder, PrintWriter output) {
        try {
            String path = ftpInboundDir+folder;
            output.write("Listing files for : " + path + "\n\n");
            Files.list(Paths.get(path))
                    .forEach(output::println);
        } catch (IOException e) {
            output.write("Error:" + e.getMessage());
        }

    }
}
