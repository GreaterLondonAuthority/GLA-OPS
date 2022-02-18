/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.messaging.Message;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import uk.gov.london.ops.payment.implementation.sap.model.SalesInvoiceDocument;

import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Stub for a finance information service.
 *
 * @author Steve Leach
 */
@Component
public class FinanceServicePOC {

    DefaultSftpSessionFactory ftpSessionFactory = new DefaultSftpSessionFactory(true);
    SftpInboundFileSynchronizingMessageSource sftpSource;

    @Value("${sap.ftp.inbound.path}")
    public String ftpInboundDir;

    private String ftpSourcePath = null;

    public void listFilesDirect(PrintWriter output) {
        SftpSession session = null;
        try {
            long start = System.currentTimeMillis();
            session = ftpSessionFactory.getSession();
            ChannelSftp.LsEntry[] files = session.list(ftpSourcePath);
            long elapsed = System.currentTimeMillis() - start;
            output.println("Remote listing complete in " + elapsed + "ms");
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
        marshaller.setPackagesToScan("uk.gov.london.ops.payment");
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
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

            String name = String.format("%s/invoice-%s.xml", this.ftpSourcePath, invoice.invoiceReferences.SuppliersInvoiceNumber);
            InputStream data = new ByteArrayInputStream(content.getBytes(UTF_8));
            session.write(data, name);

            data.close();

            output.println("File written: " + name);
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
        invoice.narrative = "";
        //invoice.amount = new BigDecimal(1234.56);
        //invoice.addLine("9876.54");

        return invoice;
    }

    public void listLocalFiles(String folder, PrintWriter output) {
        try {
            String path = ftpInboundDir + folder;
            output.write("Listing files for : " + path + "\n\n");
            Files.list(Paths.get(path))
                    .forEach(output::println);
        } catch (IOException e) {
            output.write("Error:" + e.getMessage());
        }

    }
}
