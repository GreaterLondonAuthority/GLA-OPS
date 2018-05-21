/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.Environment;
import uk.gov.london.ops.domain.attachment.AttachmentFile;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.exception.ForbiddenAccessException;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.repository.FileRepository;

import java.io.IOException;

@Service
public class FileService {

    @Autowired
    UserService userService;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    Environment environment;

    public AttachmentFile find(Integer id) {
        AttachmentFile file = fileRepository.findOne(id);

        if (file == null) {
            throw new NotFoundException();
        }

        User currentUser = userService.currentUser();
        if (!currentUser.isGla() && !currentUser.inOrganisation(file.getOrganisationId())) {
            throw new ForbiddenAccessException();
        }

        return file;
    }

    public AttachmentFile upload(Integer orgId, String name, String contentType, byte[] content) throws IOException {
        AttachmentFile file = new AttachmentFile();
        file.setOrganisationId(orgId);
        file.setContentType(contentType);
        file.setFileName(name);
        file.setFileContent(content);
        file.setCreatedOn(environment.now());
        file.setCreator(userService.currentUser());
        return fileRepository.save(file);
    }

    public void delete(Integer id) {
        fileRepository.delete(id);
    }
}
