/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.london.ops.domain.attachment.AttachmentFile;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.service.FileService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@Api(description="file api")
public class FileAPI {

    @Autowired
    FileService fileService;

    @Value("${max.file.size}")
    int maxFileSize;

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @GetMapping("/file/{id}")
    @ApiOperation(value="file download", notes="Endpoint for downloading an file")
    public @ResponseBody ResponseEntity<byte[]> download(@PathVariable Integer id) {
        AttachmentFile file = fileService.find(id);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                .body(file.getFileContent());
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @PostMapping("/file")
    @ApiOperation(value="file upload", notes="Endpoint for uploading an file")
    public @ResponseBody AttachmentFile upload(MultipartFile file, @RequestParam Integer orgId) throws IOException {
        if (file.getSize() > maxFileSize) {
            throw new ValidationException("file size cannot exceed "+(maxFileSize/(1024*1024))+"Mb");
        }
        return fileService.upload(orgId, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }

    @Secured(Role.OPS_ADMIN)
    @DeleteMapping("/file/{id}")
    public void delete(@PathVariable Integer id) {
        fileService.delete(id);
    }

}
