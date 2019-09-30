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
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.file.implementation.FileRepository;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.service.UserService;

import javax.transaction.Transactional;

@Service
@Transactional
public class FileService {

    @Autowired
    UserService userService;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    Environment environment;

    public AttachmentFile get(Integer id) {
        return fileRepository.findById(id).orElse(null);
    }

    public byte[] getFileContent(Integer id) {
        try {
            return fileRepository.getFileContent(id);
        } catch (Exception e) {

            throw new ValidationException( "Unable to find matching file content");
        }
    }

    @NotNull
    public AttachmentFile getAttachmentFile(Integer id) {
        AttachmentFile file = get(id);

        if (file == null) {
            throw new NotFoundException();
        }


        User currentUser = userService.currentUser();

        if (!currentUser.isGla()) {
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

    public AttachmentFile save(AttachmentFile file) {

        AttachmentFile saved = fileRepository.save(file);
        fileRepository.updateFileContent(saved.getId(), saved.getFileContent());
        return saved;
    }

    public AttachmentFile upload(Integer orgId, String name, String contentType, long fileSize, byte[] content) {
        AttachmentFile file = new AttachmentFile();
        file.setOrganisationId(orgId);
        file.setContentType(contentType);
        file.setFileName(name);
        file.setFileSize(fileSize);
        file.setFileContent(content);
        file.setCreatedOn(environment.now());
        file.setCreator(userService.currentUser());
        return save(file);
    }

    public void delete(Integer id) {
        fileRepository.deleteById(id);
    }

}
