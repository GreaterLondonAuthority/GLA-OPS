/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.store;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileCategory;
import uk.gov.london.ops.file.StorageOption;
import uk.gov.london.ops.framework.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This is the class responsible with connection to OwnCloud instance
 * and the service interface for uploading and deleting files from OwnCloud
 *
 * @author Carmina Matias
 */
@Component
public class OwnCloudFileStore extends AbstractFileStore {

    @Value("${owncloud.url}")
    private String ownCloudUrl;

    @Value("${owncloud.username}")
    private String ownCloudUsername;

    @Value("${owncloud.password}")
    private String ownCloudPassword;

    @Value("${env.fullname}")
    private String envFullName;

    Logger log = LoggerFactory.getLogger(getClass());

    private Sardine getSardine() {
        Sardine sardine = SardineFactory.begin();
        if (ownCloudUsername != null && ownCloudPassword != null && !ownCloudPassword.contains("password")) {
            try {
                sardine.setCredentials(ownCloudUsername, new String(Base64.getUrlDecoder().decode(ownCloudPassword.getBytes())));
            } catch (Exception e) {
                log.error("Could not login to OwnCloud. Exception:" + e.getMessage());
            }
        } else {
            log.error("Unable to login to OwnCloud, password is missing.");
        }
        return sardine;
    }

    @Override
    public StorageOption getStorageOption() {
        return StorageOption.OwnCloud;
    }

    private String generateRootPath() {
        try {
            return URLEncoder.encode(envFullName.replace(" ", "_"), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new ValidationException("UnsupportedEncoding exception: " + e.getMessage());
        }
    }

    private void createFolder(String rootPath, Sardine sardine) {
        String[] directories = rootPath.split("/");
        StringBuilder currentDir = new StringBuilder();
        for (String directory : directories) {
            currentDir.append(directory).append("/");
            try {
                if (!sardine.exists(ownCloudUrl + "/" + currentDir)) {
                    sardine.createDirectory(ownCloudUrl + "/" + currentDir);
                }
            } catch (Exception e) {
                throw new ValidationException("Something happen, the folder could not be created. Exception: " + e.getMessage());
            }
        }
    }

    @Override
    public AttachmentFile uploadFile(InputStream inputStream, String filename, long fileSize, String fileContentType,
                                     FileCategory category, Integer orgId, String directory) {
        String rootPath = generateRootPath() + "/" + directory;
        String url = ownCloudUrl + "/" + rootPath + "/";
        String generatedFileName = getUniqueFilename(filename);
        try {
            Sardine sardine = getSardine();
            createFolder(rootPath, sardine);
            if (sardine.exists(url)) {
                sardine.put(url + generatedFileName, inputStream);
            } else {
                throw new ValidationException("The path location cannot be found, path: " + rootPath);
            }
        } catch (Exception e) {
            throw new ValidationException("Something happen, the file could not be uploaded. Exception: " + e.getMessage());
        }

        String link = rootPath + "/" + generatedFileName;
        return createFile(orgId, filename, fileContentType, fileSize, category, link);
    }

    @Override
    public void getFileContent(AttachmentFile file, OutputStream out) {
        try {
            InputStream in = getFileInputStream(file.getLink());
            FileCopyUtils.copy(in, out);
        } catch (Exception e) {
            if (e instanceof SardineException && ((SardineException) e).getStatusCode() == 404) {
                throw new ValidationException(
                        "Oops, that file is no longer available. Please contact an OPS administrator for further assistance.");
            } else {
                throw new ValidationException("The file could not be downloaded: " + e.getMessage());
            }
        }
    }

    @Override
    public void getFileContentWithoutClosingStream(AttachmentFile file, OutputStream out) throws IOException {
        InputStream in = getFileInputStream(file.getLink());
        StreamUtils.copy(in, out);
        in.close();
    }

    private InputStream getFileInputStream(String link) throws IOException {
        Sardine sardine = getSardine();
        return sardine.get(ownCloudUrl + "/" + link);
    }

    //@Override
    //public void deleteFile(String filename) {
    //    try {
    //        getSardine().delete(ownCloudUrl + "/" + generateRootPath() + "/" + filename);
    //    } catch (Exception e) {
    //        throw new ValidationException("Something happen, the file could not be deleted. Exception: " + e.getMessage());
    //    }
    //}

}
