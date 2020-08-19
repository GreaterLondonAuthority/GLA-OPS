/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.framework.feature.Feature;
import uk.gov.london.ops.framework.feature.FeatureStatus;
import uk.gov.london.ops.user.UserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;

@RestController
@RequestMapping("/api/v1")
@Api
public class FileAPI {

    @Autowired
    FileService fileService;

    @Autowired
    UserService userService;

    @Autowired
    FeatureStatus featureStatus;

    @Value("${max.file.size}")
    int maxFileSize;

    @GetMapping("/file/{id}")
    @ApiOperation(value = "file download", notes = "Endpoint for downloading an file")
    public void download(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        AttachmentFile file = fileService.getAttachmentFile(id);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"");
        response.setContentType(file.getContentType());
        if (file.getFileSize() != null) {
            response.setContentLength(file.getFileSize().intValue());
        }

        fileService.getFileContent(file, response.getOutputStream());

        response.flushBuffer();
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @PostMapping("/file")
    @ApiOperation(value = "file upload", notes = "Endpoint for uploading an file")
    @ResponseBody
    public AttachmentFile upload(@RequestParam Integer orgId,
                                 @RequestParam(defaultValue = "Attachment", required = false) FileCategory category,
                                 MultipartFile file) throws IOException {
        if (file.getSize() > maxFileSize) {
            throw new ValidationException("file size cannot exceed " + (maxFileSize / (1024 * 1024)) + "Mb");
        }
        return fileService.upload(orgId, file.getOriginalFilename(), file.getContentType(), file.getSize(),
                file.getInputStream(), category);
    }

    @Secured(OPS_ADMIN)
    @DeleteMapping("/file/{id}")
    public void delete(@PathVariable Integer id) {
        fileService.delete(id);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM})
    @RequestMapping(value = "/file/{projectId}/downloadAllAnswers", produces = "application/zip", method = RequestMethod.GET)
    public void downloadAllAnswers(@PathVariable Integer projectId, HttpServletResponse response) throws IOException {

        if (featureStatus.isEnabled(Feature.AllowAllFileDownload)) {
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + "P" + projectId + ".zip\"");
            response.addHeader(HttpHeaders.CONTENT_TYPE, "application/zip");
            response.flushBuffer();
            fileService.getZipFileForProject(projectId, response.getOutputStream());
            response.flushBuffer();

        }  else {
            throw new ForbiddenAccessException("This feature is currently disabled.");
        }
    }

    @GetMapping("files")
    public List<AttachmentFile> getFiles(@RequestParam FileCategory category) {
        return fileService.getFiles(category);
    }
}
