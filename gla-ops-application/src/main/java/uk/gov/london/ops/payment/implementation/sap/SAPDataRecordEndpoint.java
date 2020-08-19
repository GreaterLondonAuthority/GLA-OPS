/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment.implementation.sap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.xml.source.DomSourceFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.payment.SapData;
import uk.gov.london.ops.payment.implementation.repository.SapDataRepository;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import static uk.gov.london.common.GlaUtils.getFileContent;

/**
 * Handler for SAP Data messages
 * Created by chris on 26/01/2017.
 */
@MessageEndpoint()
public class SAPDataRecordEndpoint {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    Environment environment;

    @Autowired
    private SapDataRepository sapDataRepository;

    @Value("${sap.ftp.delete.local.files}")
    private boolean deleteLocalFiles;

    XPathFactory factory = XPathFactory.newInstance();

    /**
     * Process the input filename, and split into individual data items, returns null if successful
     */
    @ServiceActivator(inputChannel = "sftpChannel")
    public String process(File file) throws Exception {
        if (file.getName().toLowerCase().endsWith(".xml")) {
            Long count = sapDataRepository.countByFileName(file.getName());
            if (count > 0) {
                log.warn("Ignoring duplicate file: " + file.getName());
                return null;
            }

            SapData errorData = setupSapErrorData(file);

            String content = getFileContent(file);
            if (content == null) {
                saveError("Unable to read file content", "Error!", errorData);
                return null;
            }

            errorData.setContent(content);

            Node root = loadXmlFile(content);
            if (root == null) {
                saveError("Unable to read parse XML", "Error!", errorData);
                return null;
            }

            String interfaceType = null;
            List<String> messages = null;
            try {
                interfaceType = getInterfaceType(root);
                messages = splitMessage(root);
            } catch (XPathExpressionException e) {
                saveError("Unable to split XML", "Error!", errorData);
                return null;
            }

            if (messages != null) {
                if (messages.size() == 0) {
                    saveError("File contains no items", "Empty!", errorData);
                }

                int segmentNumber = 1;
                for (String message : messages) {
                    saveSegment(interfaceType, file.getName(), segmentNumber++, message);
                }
                log.debug("Processed SAP Data File: " + file.getName());

                if (deleteLocalFiles && file.delete()) {
                    log.debug("Successfully deleted file {}", file.getName());
                }
                else {
                    log.warn("Failed to delete file {} after being processed!", file.getName());
                }
            }
        } else {
            log.warn("SAP interface ignoring non-XML file " + file.getName());
        }
        return null;
    }

    private void saveError(String errorSummary, String category, SapData errorData) {
        errorData.setErrorDescription(errorSummary);
        errorData.setInterfaceType(category);
        sapDataRepository.save(errorData);
    }

    private void saveSegment(String interfaceType, String fileName, int segmentNumber, String content) {
        SapData data = new SapData();
        data.setContent(content);
        data.setCreatedOn(environment.now());
        data.setProcessed(false);
        data.setSegmentNumber(segmentNumber);
        data.setFileName(fileName);
        data.setInterfaceType(interfaceType);
        sapDataRepository.save(data);
    }

    private SapData setupSapErrorData(File file) {
        SapData errorData = new SapData();
        errorData.setFileName(file.getName());
        errorData.setProcessed(true);
        errorData.setInterfaceType("Error");
        errorData.setCreatedOn(environment.now());
        return errorData;
    }

    protected String getInputFileName(String fileName) {
        if (fileName.indexOf('/') != -1) {
            return fileName.substring(fileName.lastIndexOf('/') + 1);
        }
        return fileName;
    }

    private String getInterfaceType(Node root) throws XPathExpressionException {
        return (String) factory.newXPath().evaluate("//header//interfaceType", root, XPathConstants.STRING);
    }

    protected List<String> splitMessage(Node root) throws XPathExpressionException {
        XPath xpath = factory.newXPath();

        List<String> lines = new LinkedList<>();

        NodeList nodes = (NodeList)xpath.evaluate("//data",root, XPathConstants.NODESET);
        for (int n = 0; n < nodes.getLength(); n++) {
            Node data = nodes.item(n);
            lines.add(nodeToString(data));
        }
        return lines;
    }

    private Node loadXmlFile(String xmlText) {
        try {
            DOMSource source = (DOMSource) (new DomSourceFactory().createSource(xmlText));
            return source.getNode();
        } catch (Exception e) {
            log.error("Could not load XML DOM from string", e);
            return null;
        }
    }

    private String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException e) {
            log.error("Error processing message", e);
        }
        return sw.toString();
    }
}
