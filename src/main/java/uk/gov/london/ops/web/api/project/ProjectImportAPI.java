/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.domain.importdata.ImportErrorLog;
import uk.gov.london.ops.domain.importdata.ImportJobType;
import uk.gov.london.ops.service.ImportLogService;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.service.project.ProjectImportService;
import uk.gov.london.ops.web.model.project.FileImportResult;

import java.io.IOException;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;

/**
 * REST API for managing Projects.
 *
 * @author Steve Leach
 */
@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing importing Project data"
)
public class ProjectImportAPI {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProjectImportService importService;

    @Autowired
    private FeatureStatus featureStatus;

    @Autowired
    private ImportLogService importLogService;

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/pcsImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API updloading the PCS csv file", hidden = true)
    public FileImportResult importPcsProjectFile(MultipartFile file) throws IOException {
        return importService.importPcsProjectFile(file.getInputStream());
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/imsImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the IMS csv file", hidden = true)
    public FileImportResult importImsProjectFile(MultipartFile file) throws IOException {
        if (featureStatus.isEnabled(Feature.ImsImport)) {
            FileImportResult fileImportResult = new FileImportResult();
            try {
                fileImportResult = importService.importImsProjectFile(file.getInputStream());
            } catch (IOException e) {
                log.error("Error during import" , e );
            } finally {
                List<ImportErrorLog> errors = importLogService.findAllErrorsByImportType(ImportJobType.IMS_PROJECT_IMPORT);
                fileImportResult.setErrors(errors);
                return fileImportResult;
            }
        } else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/importIMSAnswerCorrections", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the IMS answer corrections file", hidden = true)
    public FileImportResult importImsAnswerCorrections(MultipartFile file) throws IOException {
        if (featureStatus.isEnabled(Feature.ImsImport)) {
            FileImportResult fileImportResult = new FileImportResult();
            try {
                fileImportResult = importService.importImsAnswerCorrections(file.getInputStream());
            } catch (IOException e) {
                log.error("Error during import" , e );
            } finally {
                List<ImportErrorLog> errors = importLogService.findAllErrorsByImportType(ImportJobType.IMS_ANSWER_CORRECTIONS_IMPORT);
                fileImportResult.setErrors(errors);
                return fileImportResult;
            }
        } else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/imsUnitDetailsImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the IMS unit details file", hidden = true)
    public FileImportResult importImsUnitDetailsFile(MultipartFile file) throws IOException {
        if (featureStatus.isEnabled(Feature.ImsImport)) {
            FileImportResult fileImportResult = new FileImportResult();
            try {
                fileImportResult =importService.importImsUnitDetailsFile(file.getInputStream());
            } catch (IOException e) {
                log.error("Error during import" , e);
            } finally {
                List<ImportErrorLog> errors = importLogService.findAllErrorsByImportType(ImportJobType.IMS_UNIT_DETAILS_IMPORT);
                fileImportResult.setErrors(errors);
                return fileImportResult;
            }
        } else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/projects/imsClaimedUnitImport", method = RequestMethod.POST)
    @ApiOperation(value = "Internal API uploading the IMS claimed units file", hidden = true)
    public FileImportResult importImsClaimedUnitsFile(MultipartFile file) {
        if (featureStatus.isEnabled(Feature.ImsImport)) {
            FileImportResult fileImportResult = new FileImportResult();
            try {
                fileImportResult = importService.importImsClaimedUnitsFile(file.getInputStream());
            } catch (IOException e) {
                log.error("Error during import" , e);
            } finally {
                List<ImportErrorLog> errors = importLogService.findAllErrorsByImportType(ImportJobType.IMS_CLAIMED_UNITS_IMPORT);
                fileImportResult.setErrors(errors);
                return fileImportResult;
            }
        } else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }


}
