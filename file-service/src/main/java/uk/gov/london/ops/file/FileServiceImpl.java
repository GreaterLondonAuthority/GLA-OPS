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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.file.implementation.FileRepository;
import uk.gov.london.ops.file.implementation.FileRepositoryHelper;
import uk.gov.london.ops.framework.environment.Environment;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.user.User;
import uk.gov.london.ops.user.UserUtils;

@Service
@Transactional
public class FileServiceImpl implements FileService {

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

    @Value("${accepted.file.types}")
    String acceptedFileExtensions;

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

    public Set<AttachmentFile> getAllAttachmentsForProject(Integer projectId) throws IOException {
        return fileRepository.getAllAttachmentsForProject(projectId);
    }

    @NotNull
    public AttachmentFile getAttachmentFile(Integer id) {
        AttachmentFile file = get(id);
        if (file == null) {
            throw new NotFoundException();
        }
        return file;
    }

    void validateAccess(AttachmentFile file) {
        User currentUser = UserUtils.currentUser();
        if (file.getCategory() != FileCategory.Icon && !currentUser.isGla()) {
            // reports could have null org
            if (file.getOrganisationId() == null) {
                if (!currentUser.getUsername().equals(file.getCreator())) {
                    throw new ForbiddenAccessException();
                }
            } else if (!currentUser.inOrganisation(file.getOrganisationId())) {
                throw new ForbiddenAccessException();
            }
        }
    }

    public AttachmentFile save(AttachmentFile file, InputStream content) throws IOException {
        AttachmentFile saved = fileRepository.save(file);
        fileRepository.flush();
        fileRepositoryHelper.saveBinaryFile(saved.getId(), content);
        return saved;
    }

    private String buildDirectoryNameToStoreFile(Integer programmeId, Integer projectId, Integer blockId) {
        if(programmeId != null && projectId != null && blockId != null){
            return String.format("%d/%d/%d", programmeId, projectId, blockId);
        } else {
            return null;
        }
    }

    public AttachmentFile upload(Integer orgId, String name, String contentType, long fileSize, InputStream content,
                                 FileCategory category, Integer programmeId, Integer projectId, Integer blockId) throws IOException {
        String directory = buildDirectoryNameToStoreFile(programmeId, projectId, blockId);
        return upload(orgId, name, contentType, fileSize, content, category, directory);
    }

    public AttachmentFile upload(Integer orgId, String name, String contentType, long fileSize, InputStream content,
                                 FileCategory category, String directory) throws IOException {

        if (!acceptedFileExtensions.contains(getFileExtension(name))) {
            throw new ValidationException("Please upload a file in one of the following formats: " + acceptedFileExtensions);
        }
        Pattern symbols = Pattern.compile("[#$%&|<>‘ “ ?{}/\\[\\]~—]");
        Matcher hasSymbols = symbols.matcher(name.replaceAll("\\s+",""));
        if (hasSymbols.find() || name.contains("\\")) {
            throw new ValidationException("Cannot upload files with following symbols: ~ / \\ | [] {} ‘ “ \" ? & # $ % <> —. "
                    + "Please amend the file name and try again.");
        }

        User currentUser = UserUtils.currentUser();
        if (category == FileCategory.Icon && !currentUser.isOpsAdmin()) {
            throw new ValidationException("Icons can only be uploaded by OPS Admin");
        }

        // Before uploading, we check the storage option selected and we save file to selected option
        StorageOption storageOption = StorageOption.valueOf(environment.storageOption());

        directory = directory != null ? directory : "";
        return getFileStore(storageOption).uploadFile(content, name, fileSize, contentType, category, orgId, directory);
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
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
