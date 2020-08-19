/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.framework.Environment;
import uk.gov.london.ops.file.implementation.FileRepository;
import uk.gov.london.ops.file.implementation.FileRepositoryHelper;
import uk.gov.london.ops.file.store.FileStore;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.service.DataAccessControlService;
import uk.gov.london.ops.user.UserService;
import uk.gov.london.ops.user.domain.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class FileService {

    @Autowired
    UserService userService;

    @Autowired
    DataAccessControlService dataAccessControlService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    FileRepository fileRepository;

    @Autowired(required = false)
    FileStore[] fileStores;

    @Autowired
    Environment environment;

    @Autowired
    FileRepositoryHelper fileRepositoryHelper;

    public FileStore getFileStore(StorageOption storageOption) {
        for (FileStore fileStore : fileStores) {
            if (fileStore.getStorageOption().equals(storageOption)) {
                return fileStore;
            }
        }
        throw new ValidationException("Unknown file storage " + storageOption);
    }

    public AttachmentFile get(Integer id) {
        return fileRepository.findById(id).orElse(null);
    }

    public void getFileContent(AttachmentFile file, OutputStream out) {
        try {
            getFileStore(file.getStorageLocation()).getFileContent(file, out);
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public void getZipFileForProject(Integer projectId, OutputStream out) throws IOException {

        dataAccessControlService.checkProjectAccess(projectId);

        Set<AttachmentFile> attachments = fileRepository.getAllAttachmentsForProject(projectId);
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out))) {
            for (AttachmentFile attachment : attachments) {
                ZipEntry zipEntry = new ZipEntry(attachment.getId() + "-" + attachment.getFileName());
                zos.putNextEntry(zipEntry);
                getFileStore(attachment.getStorageLocation()).getFileContentWithoutClosingStream(attachment, zos);
                zos.closeEntry();
            }
            zos.flush();
        }
    }

    @NotNull
    public AttachmentFile getAttachmentFile(Integer id) {
        AttachmentFile file = get(id);

        if (file == null) {
            throw new NotFoundException();
        }

        User currentUser = userService.currentUser();

        if (file.getCategory() != FileCategory.Icon && !currentUser.isGla()) {
            // reports could have null org
            if (file.getOrganisationId() == null) {
                if (!currentUser.getUsername().equals(file.getCreator().getUsername())) {
                    throw new ForbiddenAccessException();
                }
            } else if (!currentUser.inOrganisation(file.getOrganisationId())) {
                throw new ForbiddenAccessException();
            }
        }
        return file;
    }

    public AttachmentFile save(AttachmentFile file, InputStream content) throws IOException {
        AttachmentFile saved = fileRepository.save(file);
        fileRepository.flush();
        fileRepositoryHelper.saveBinaryFile(saved.getId(), content);
        return saved;
    }

    public AttachmentFile upload(Integer orgId, String name, String contentType, long fileSize, InputStream content,
                                 FileCategory category) throws IOException {
        return upload(orgId, name, contentType, fileSize, content, category, null);
    }

    public AttachmentFile upload(Integer orgId, String name, String contentType, long fileSize, InputStream content,
                                 FileCategory category, String directory) throws IOException {
        User currentUser = userService.currentUser();
        if (category == FileCategory.Icon && !currentUser.isOpsAdmin()) {
            throw new ValidationException("Icons can only be uploaded by OPS Admin");
        }

        // Before uploading, we check the storage option selected and we save file to selected option
        StorageOption storageOption = StorageOption.valueOf(environment.storageOption());

        directory = directory != null ? directory : "";
        return getFileStore(storageOption).uploadFile(content, name, fileSize, contentType, category, orgId, directory);
    }

    public void delete(Integer id) {
        fileRepository.deleteById(id);
    }

    public List<AttachmentFile> getFiles(FileCategory category) {
        if (FileCategory.Icon == category) {
            return getIcons();
        }
        throw new ValidationException("Only " + FileCategory.Icon + "category is supported at the moment");
    }

    public AttachmentFile getIcon(String iconName) {
        return fileRepository.findByFileNameAndCategory(iconName, FileCategory.Icon);
    }

    public List<AttachmentFile> getIcons() {
        return fileRepository.findAllByCategory(FileCategory.Icon);
    }
}
