/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.store;

import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileCategory;
import uk.gov.london.ops.file.StorageOption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for External File Storage modules.
 *
 */
public interface FileStore {

    StorageOption getStorageOption();

    AttachmentFile uploadFile(InputStream is, String name, long size, String contentType, FileCategory category,
                              Integer orgId, String directory) throws IOException;

    void getFileContent(AttachmentFile file, OutputStream out);

    void getFileContentWithoutClosingStream(AttachmentFile file, OutputStream out) throws IOException;

    void deleteFile(String filename);

}
