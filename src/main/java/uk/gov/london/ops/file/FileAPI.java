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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.service.UserService;

import java.io.IOException;

import static uk.gov.london.common.user.BaseRole.*;

@RestController
@RequestMapping("/api/v1")
@Api(description="file api")
public class FileAPI {

    @Autowired
    FileService fileService;

    @Autowired
    UserService userService;

    @Value("${max.file.size}")
    int maxFileSize;

    @Value("${threshold.file.size}")
    long thresholdFileSize;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @GetMapping("/file/{id}")
    @ApiOperation(value="file download", notes="Endpoint for downloading an file")
    public @ResponseBody ResponseEntity<byte[]> download(@PathVariable Integer id) {
        AttachmentFile file = fileService.getAttachmentFile(id);
        User user = userService.currentUser();
        if (file.getFileSize() != null && file.getFileSize() > thresholdFileSize && !user.hasRole(OPS_ADMIN)) {
            throw new ValidationException("This file is currently too large to download, please contact a GLA Ops Admin");
        }
        byte[] fileContent = fileService.getFileContent(id);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                .body(fileContent);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @PostMapping("/file")
    @ApiOperation(value="file upload", notes="Endpoint for uploading an file")
    public @ResponseBody AttachmentFile upload(MultipartFile file, @RequestParam Integer orgId) throws IOException {
        if (file.getSize() > maxFileSize) {
            throw new ValidationException("file size cannot exceed "+(maxFileSize/(1024*1024))+"Mb");
        }
        return fileService.upload(orgId, file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getBytes());
    }

    @Secured(OPS_ADMIN)
    @DeleteMapping("/file/{id}")
    public void delete(@PathVariable Integer id) {
        fileService.delete(id);
    }

}
