/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.finance;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
//import uk.gov.london.ops.di.PCSDataImporter;
import uk.gov.london.ops.domain.refdata.FinanceCategory;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.integration.poc.FinanceServicePOC;
import uk.gov.london.ops.sap.MoveItSynchroniser;
import uk.gov.london.ops.service.FinanceCategoryService;
import uk.gov.london.ops.service.finance.FinanceService;
import uk.gov.london.ops.service.finance.FinancialCalendar;
import uk.gov.london.ops.service.finance.SapDataService;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static uk.gov.london.ops.domain.importdata.ImportJobType.*;

/**
 * Proof-of-concept Finance API
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1")
@Api(description="Finance integration API.")
public class FinanceAPI {

    @Autowired
    FinanceServicePOC service;

    @Autowired
    FinanceService financeService;

    @Autowired
    FinancialCalendar financialCalendar;

    @Autowired
    SapDataService sapDataService;

    @Autowired
    FinanceCategoryService financeCategoryService;

    @Resource(name="actualsSynchroniser")
    MoveItSynchroniser moveItSynchroniser;

//    @Autowired
//    PCSDataImporter pcsDataImporter;

    @Value("${sap.ftp.inbound.path}")
    public String ftpInboundDir;


    @RequestMapping(value = "/finance/poc/read", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    @Secured(Role.OPS_ADMIN)
    public String sapRead(
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "timeout", defaultValue = "5000") Integer timeout,
            @RequestParam(name = "host", defaultValue = "") String host,
            @RequestParam(name = "user", defaultValue = "") String user,
            @RequestParam(name = "port", defaultValue = "") Integer port,
            @RequestParam(name = "sync", defaultValue = "sync") String mode,
            @RequestParam(name = "path", defaultValue = "") String path) throws IOException {

        service.configureFTP(host, port, path, user, password, timeout);

        StringWriter sw = new StringWriter();

        PrintWriter output = new PrintWriter(sw);

        if (mode.equalsIgnoreCase("direct")) {
            displayParamConfiguration(host, user, port, path, output);
            service.listFilesDirect(output);
        } else if (mode.equalsIgnoreCase("prod")) {
            displayParamConfiguration(host, user, port, path, output);
            sapDataService.processSapData();
        } else if (mode.equalsIgnoreCase("synctest")) {
            moveItSynchroniserTest(output);
        } else {
            displayParamConfiguration(host, user, port, path, output);
            service.syncAndProcessFiles(output);
        }

        return sw.getBuffer().toString();
    }

    private void displayParamConfiguration(String host, String user, Integer port, String path, PrintWriter output) {
        output.println("Session factory initialised");
        output.println("Host = " + host);
        output.println("Port = " + port);
        output.println("User = " + user);
        output.println("Path = " + path);
    }

    private void moveItSynchroniserTest(PrintWriter out) {
        out.write("Using preconfigured synchroniser, ignoring config from API call\n");
        out.write("Remote directory: " + moveItSynchroniser.getRemoteDirectory() + "\n\n");

        try {
            List<String> remoteFileList = moveItSynchroniser.getRemoteFileList();
            for (String file : remoteFileList) {
                out.write(file);
                out.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    @RequestMapping(value = "/finance/poc/listLocal", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    @Secured(Role.OPS_ADMIN)
    public String sapListRemote(@RequestParam(required = false, defaultValue = "") String folder) throws IOException {


        StringWriter sw = new StringWriter();
        PrintWriter output = new PrintWriter(sw);
        service.listLocalFiles(folder, output);
        return sw.getBuffer().toString();
    }

    @RequestMapping(value = "/finance/poc/write", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    @Secured(Role.OPS_ADMIN)
    public String sapWrite(
            @RequestParam("password") String password,
            @RequestParam(name = "timeout", defaultValue = "5000") Integer timeout,
            @RequestParam(name = "host", defaultValue = "") String host,
            @RequestParam(name = "user", defaultValue = "") String user,
            @RequestParam(name = "port", defaultValue = "") Integer port,
            @RequestParam(name = "path", defaultValue = "") String path) {

        StringWriter sw = new StringWriter();

        service.configureFTP(host, port, path, user, password, timeout);

        PrintWriter output = new PrintWriter(sw);
        displayParamConfiguration(host, user, port, path, output);
        output.println("Session factory initialised");

        service.sendSapTestFile(output);

        return sw.getBuffer().toString();
    }


    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/poc/forcesync", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    public String forceSync() throws IOException {
        moveItSynchroniser.sync();
        return "Synchronisation forced";
    }


    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/toggleSFTPTask", method = RequestMethod.GET)
    @ApiOperation(value = "internal tool to disable the SFTP sync job.", hidden = true)
    public String toggleSFTPTask(@RequestParam("paused") boolean paused) throws IOException {
        moveItSynchroniser.setPaused(paused);
        return "Synchroniser is paused: " + moveItSynchroniser.isPaused();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/sapFile", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API for uploading samaple XML file", hidden = true)
    public void createSapFile(MultipartFile file) throws IOException {
        if (ftpInboundDir == null) {
            throw new IllegalStateException("No SAP inbound directory configured");
        }
        new File(ftpInboundDir).mkdirs();
        if (file == null) {
            throw new ValidationException("File parameter is required");
        }

        file.transferTo(new File(ftpInboundDir+file.getOriginalFilename()));
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    @RequestMapping(value = "/finance/categories", method = RequestMethod.GET)
    @ApiOperation(value = "get the finance categories", notes = "")
    public List<FinanceCategory> getFinanceCategories() {
        return financeCategoryService.getFinanceCategories();
    }

    @RequestMapping(value = "/finance/currentFinancialYear", method = RequestMethod.GET)
    @ApiOperation(value = "returns current financial year as an integer", notes = "")
    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR, Role.TECH_ADMIN})
    public Integer getCurrentFinancialYear() {
        return financialCalendar.currentFinancialYear();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/pcsImport", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Import legacy PCS transactions", hidden = true)
    public void importPcsTransactions(MultipartFile file) throws Exception {
//        pcsDataImporter.importPcsElements(file.getInputStream(), PCS_TRANSACTION_IMPORT);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/receiptsImport", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Import legacy PCS receipts", hidden = true)
    public void importPcsReceipts(MultipartFile file) throws Exception {
//        pcsDataImporter.importPcsElements(file.getInputStream(), PCS_RECEIPT_IMPORT);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/outputsImport", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Import legacy PCS outputs", hidden = true)
    public void importPcsOutputs(MultipartFile file) throws Exception {
//        pcsDataImporter.importPcsElements(file.getInputStream(), PCS_OUTPUT_IMPORT);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/updateFromOriginalLedgerEntry", method = RequestMethod.PUT)
    public void updateFromOriginalLedgerEntry(@RequestParam Integer originalId, @RequestParam Integer copyId) {
        financeService.updateFromOriginalLedgerEntry(originalId, copyId);
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/updateLedgerEntriesFromOriginal", method = RequestMethod.PUT)
    public void updateLedgerEntriesFromOriginal() {
        financeService.updateLedgerEntriesFromOriginal();
    }

    @Secured(Role.OPS_ADMIN)
    @RequestMapping(value = "/finance/importLedgerEntries", method = RequestMethod.POST)
    public void importLedgerEntries(MultipartFile file) throws IOException {
        financeService.importLedgerEntries(file.getInputStream());
    }

}
