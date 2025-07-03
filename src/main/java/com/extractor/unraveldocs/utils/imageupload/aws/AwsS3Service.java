package com.extractor.unraveldocs.utils.imageupload.aws;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.utils.imageupload.FileFolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Getter
    @Value("${aws.s3.cloudfront.url}")
    private String awsS3CloudFrontUrl;

    @Getter
    private static final String PROFILE_PICTURE_FOLDER = FileFolder.PROFILE_PICTURE.getFolder();

    @Getter
    private static final String DOCUMENT_PICTURE_FOLDER = FileFolder.DOCUMENT_PICTURE.getFolder();

    public String uploadFile(MultipartFile file, String fileName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return "https://" + awsS3CloudFrontUrl + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public String generateFileName(String originalFileName, String folderName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new BadRequestException("Original file name cannot be null or empty");
        }
        return folderName + UUID.randomUUID() + "-" + originalFileName.split("\\.")[0].replaceAll("[^a-zA-Z0-9]",
                "_").trim() + "." + originalFileName.split("\\.")[1];
    }

    public String generateRandomPublicId(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new BadRequestException("Original file name cannot be null or empty");
        }
        return UUID.randomUUID() + "-" + originalFileName.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String key = fileUrl.substring(("https://" + awsS3CloudFrontUrl + "/").length());

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception ex) {
            log.error("Error deleting file from S3: {}", ex.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to delete file from S3", ex);
        }
    }

}
