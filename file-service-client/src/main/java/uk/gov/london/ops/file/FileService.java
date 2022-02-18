/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public interface FileService {

    AttachmentFile get(Integer id);

    AttachmentFile getIcon(String iconName);

    Set<AttachmentFile> getAllAttachmentsForProject(Integer projectId) throws IOException;

    AttachmentFile getAttachmentFile(Integer id);

    FileStore getFileStore(StorageOption storageOption);

    void getFileContent(AttachmentFile file, OutputStream out);

    AttachmentFile save(AttachmentFile file, InputStream content) throws IOException;

    AttachmentFile upload(Integer orgId, String name, String contentType, long fileSize, InputStream content,
                          FileCategory category, Integer programmeId, Integer projectId, Integer blockId) throws IOException;

    AttachmentFile upload(Integer orgId, String name, String contentType, long fileSize, InputStream content,
                          FileCategory category, String directory) throws IOException;
}
