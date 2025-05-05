package com.extractor.unraveldocs.utils.imageupload.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

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

            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            URL url = s3Client.utilities().getUrl(getUrlRequest);
            return url.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public String generateFileName(String originalFileName) {
        return "profile_pictures/" + UUID.randomUUID() + "-" + originalFileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/profile_pictures/") + 1);
            log.info("Deleting file from S3: {}", fileName);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key("profile_pictures/" + fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", fileName);

        } catch (S3Exception ex) {
            log.error("Error deleting file from S3: {}", ex.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to delete file from S3", ex);
        }
    }

}
