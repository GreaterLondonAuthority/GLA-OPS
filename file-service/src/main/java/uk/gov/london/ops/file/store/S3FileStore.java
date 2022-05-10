/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.store;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
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

/**
 * This is the class responsible with connection to S3 bucket
 * and the service interface for uploading and deleting files from S3
 *
 * @author Carmina Matias
 */
@Component
public class S3FileStore extends AbstractFileStore {

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Value("${env.fullname}")
    private String envFullName;

    public static final Integer FILE_SIZE_LIMIT  = 524288000;

    Logger log = LoggerFactory.getLogger(getClass());

    private AmazonS3 getS3Client() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.EU_WEST_2)
                .build();
    }

    @Override
    public StorageOption getStorageOption() {
        return StorageOption.S3;
    }

    @Override
    public AttachmentFile uploadFile(InputStream inputStream, String filename, long fileSize, String fileContentType,
                                     FileCategory category, Integer orgId, String directory) {
        String fileKey = envFullName + "/" + directory + "/" + getUniqueFilename(filename);

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(fileContentType);

            if (fileSize > FILE_SIZE_LIMIT) {
                multipartUploadFileToS3Bucket(fileKey, inputStream, metadata);
            } else {
                uploadFileToS3Bucket(fileKey, inputStream, metadata);
            }
        } catch (Exception e) {
            throw new ValidationException("Something happen, the file could not be uploaded. Exception: " + e.getMessage());
        }

        return createFile(orgId, filename, fileContentType, fileSize, category, fileKey);
    }

    private void uploadFileToS3Bucket(String fileName, InputStream inputStream, ObjectMetadata metadata) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
        getS3Client().putObject(putObjectRequest);
    }

    private void multipartUploadFileToS3Bucket(String fileName, InputStream inputStream, ObjectMetadata metadata) {
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(getS3Client())
                .build();

        try {
            // TransferManager processes all transfers asynchronously,
            // so this call returns immediately.
            Upload upload = tm.upload(bucketName, fileName, inputStream, metadata);
            log.debug("Object upload started");

            // Optionally, wait for the upload to finish before continuing.
            upload.waitForCompletion();
            log.debug("Object upload complete");
        } catch (AmazonServiceException | InterruptedException e) {
            log.error("Error uploading file: {}", e.toString());
        } finally {
            tm.shutdownNow();
        }
    }

    @Override
    public void getFileContent(AttachmentFile file, OutputStream out) {
        try {
            S3ObjectInputStream fileStream = getFileInputStream(file.getLink());
            FileCopyUtils.copy(fileStream, out);
        } catch (Exception e) {
            throw new ValidationException("Something happen, the file could not be downloaded. Exception: " + e.getMessage());
        }
    }

    @Override
    public void getFileContentWithoutClosingStream(AttachmentFile file, OutputStream out) throws IOException {
        S3ObjectInputStream fileStream = getFileInputStream(file.getLink());
        StreamUtils.copy(fileStream, out);
        fileStream.close();
    }

    private S3ObjectInputStream getFileInputStream(String link) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, link);
        S3Object file = getS3Client().getObject(getObjectRequest);
        return file.getObjectContent();
    }

    //public void deleteFile(String fileName) {
    //    try {
    //        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
    //        getS3Client().deleteObject(deleteObjectRequest);
    //    } catch (Exception e) {
    //        throw new ValidationException("Something happen, the file could not be deleted. Exception: " + e.getMessage());
    //    }
    //}
}
