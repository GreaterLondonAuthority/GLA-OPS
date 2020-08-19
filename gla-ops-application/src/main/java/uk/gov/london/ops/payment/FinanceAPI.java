/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.payment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.framework.calendar.FinancialCalendar;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.payment.implementation.sap.MoveItSynchroniser;
import uk.gov.london.ops.refdata.FinanceCategory;
import uk.gov.london.ops.refdata.FinanceCategoryService;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;

/**
 * Proof-of-concept Finance API
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1")
@Api("Finance integration API.")
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

    @Resource(name = "actualsSynchroniser")
    MoveItSynchroniser moveItSynchroniser;

    @Value("${sap.ftp.inbound.path}")
    public String ftpInboundDir;


    @RequestMapping(value = "/finance/poc/read", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    @Secured(OPS_ADMIN)
    public String sapRead(
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "timeout", defaultValue = "5000") Integer timeout,
            @RequestParam(name = "host", defaultValue = "moveft.tfl.gov.uk") String host,
            @RequestParam(name = "user", defaultValue = "glaops") String user,
            @RequestParam(name = "port", defaultValue = "22") Integer port,
            @RequestParam(name = "sync", defaultValue = "sync") String mode,
            @RequestParam(name = "path", defaultValue = "/Home/eP2P_Project1/GLAOPS/Outgoing/Dev/") String path)
        throws IOException {

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
    @Secured(OPS_ADMIN)
    public String sapListRemote(@RequestParam(required = false, defaultValue = "") String folder) {


        StringWriter sw = new StringWriter();
        PrintWriter output = new PrintWriter(sw);
        service.listLocalFiles(folder, output);
        return sw.getBuffer().toString();
    }

    @RequestMapping(value = "/finance/poc/write", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    @Secured(OPS_ADMIN)
    public String sapWrite(
            @RequestParam("password") String password,
            @RequestParam(name = "timeout", defaultValue = "5000") Integer timeout,
            @RequestParam(name = "host", defaultValue = "moveft.tfl.gov.uk") String host,
            @RequestParam(name = "user", defaultValue = "glaops") String user,
            @RequestParam(name = "port", defaultValue = "22") Integer port,
            @RequestParam(name = "path", defaultValue = "/Home/eP2P_Project1/GLAOPS/Incoming/Dev/invoices") String path) {

        StringWriter sw = new StringWriter();

        service.configureFTP(host, port, path, user, password, timeout);

        PrintWriter output = new PrintWriter(sw);
        displayParamConfiguration(host, user, port, path, output);
        output.println("Session factory initialised");

        service.sendSapTestFile(output);

        return sw.getBuffer().toString();
    }


    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/poc/forcesync", method = RequestMethod.GET)
    @ApiOperation(value = "POC for finance integration.", hidden = true)
    public String forceSync() {
        moveItSynchroniser.sync();
        return "Synchronisation forced";
    }


    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/toggleSFTPTask", method = RequestMethod.GET)
    @ApiOperation(value = "internal tool to disable the SFTP sync job.", hidden = true)
    public String toggleSFTPTask(@RequestParam("paused") boolean paused) {
        moveItSynchroniser.setPaused(paused);
        return "Synchroniser is paused: " + moveItSynchroniser.isPaused();
    }

    @Secured(OPS_ADMIN)
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

        file.transferTo(new File(ftpInboundDir + file.getOriginalFilename()));
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
        TECH_ADMIN})
    @RequestMapping(value = "/finance/categories", method = RequestMethod.GET)
    @ApiOperation(value = "get the finance categories")
    public List<FinanceCategory> getFinanceCategories() {
        return financeCategoryService.getFinanceCategories();
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/finance/categories", method = RequestMethod.POST)
    @ApiOperation(value = "create new finance category")
    public FinanceCategory createFinanceCategory(@RequestBody FinanceCategory category) {
        return financeCategoryService.createFinanceCategory(category);
    }

    @Secured({OPS_ADMIN, TECH_ADMIN})
    @RequestMapping(value = "/finance/categories/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "edit an existing finance category")
    public FinanceCategory editFinanceCategory(@PathVariable Integer id, @RequestBody FinanceCategory category) {
        return financeCategoryService.updateFinanceCategory(id, category);
    }

    @RequestMapping(value = "/finance/currentFinancialYear", method = RequestMethod.GET)
    @ApiOperation(value = "returns current financial year as an integer")
    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
        TECH_ADMIN})
    public Integer getCurrentFinancialYear() {
        return financialCalendar.currentYear();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/updateFromOriginalLedgerEntry", method = RequestMethod.PUT)
    public void updateFromOriginalLedgerEntry(@RequestParam Integer originalId, @RequestParam Integer copyId) {
        financeService.updateFromOriginalLedgerEntry(originalId, copyId);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/updateLedgerEntriesFromOriginal", method = RequestMethod.PUT)
    public void updateLedgerEntriesFromOriginal() {
        financeService.updateLedgerEntriesFromOriginal();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/finance/importLedgerEntries", method = RequestMethod.POST)
    public void importLedgerEntries(MultipartFile file) throws IOException {
        financeService.importLedgerEntries(file.getInputStream());
    }

}
