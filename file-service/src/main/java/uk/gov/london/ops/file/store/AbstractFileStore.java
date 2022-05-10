/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.store;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileCategory;
import uk.gov.london.ops.file.FileStore;
import uk.gov.london.ops.file.implementation.FileRepository;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.OPSUtils;

public abstract class AbstractFileStore implements FileStore {

    @Autowired
    protected Environment environment;

    @Autowired
    protected FileRepository fileRepository;

    protected AttachmentFile createFile(Integer orgId, String name, String contentType, long fileSize,
                                        FileCategory category, String link) {
        AttachmentFile file = new AttachmentFile();
        file.setOrganisationId(orgId);
        file.setContentType(contentType);
        file.setFileName(name);
        file.setFileSize(fileSize);
        file.setCategory(category);
        file.setCreatedOn(environment.now());
        file.setCreator(OPSUtils.currentUsername());
        file.setStorageLocation(getStorageOption());
        file.setLink(link);
        AttachmentFile saved = fileRepository.save(file);
        fileRepository.flush();
        return saved;
    }

    String getUniqueFilename(String filename) {
        int fileExtensionIndex = filename.lastIndexOf(".");
        if (fileExtensionIndex == -1) {
            fileExtensionIndex = filename.length();
        }
        StringBuilder sb = new StringBuilder(filename.replace(" ", "_"));
        sb.insert(fileExtensionIndex, "-" + environment.clock().millis());
        return sb.toString();
    }

}
