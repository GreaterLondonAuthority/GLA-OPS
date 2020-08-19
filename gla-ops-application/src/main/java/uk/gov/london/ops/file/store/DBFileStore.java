/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileCategory;
import uk.gov.london.ops.file.StorageOption;
import uk.gov.london.ops.file.implementation.FileRepositoryHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class DBFileStore extends AbstractFileStore {

    @Autowired
    FileRepositoryHelper fileRepositoryHelper;

    @Override
    public StorageOption getStorageOption() {
        return StorageOption.Database;
    }

    @Override
    public AttachmentFile uploadFile(InputStream content, String name, long size, String contentType,
                                     FileCategory category, Integer orgId, String directory) throws IOException {
        AttachmentFile file = createFile(orgId, name, contentType, size, category, null);
        fileRepositoryHelper.saveBinaryFile(file.getId(), content);
        return file;
    }

    @Override
    public void getFileContent(AttachmentFile file, OutputStream out) {
        fileRepositoryHelper.getBinaryFile(file.getId(), out);
    }

    @Override
    public void getFileContentWithoutClosingStream(AttachmentFile file, OutputStream out) {
        fileRepositoryHelper.getBinaryFileWithoutClosingOutputStream(file.getId(), out);
    }

    @Override
    public void deleteFile(String filename) {
    }

}
