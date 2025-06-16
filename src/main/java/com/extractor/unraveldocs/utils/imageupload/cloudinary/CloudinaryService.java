package com.extractor.unraveldocs.utils.imageupload.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    @Getter
    private static final String PROFILE_PICTURE_FOLDER = "profile_pictures";
    @Getter
    private static final String RESOURCE_TYPE_IMAGE = "image";
    private final Cloudinary cloudinary;

    public String uploadFile(
            MultipartFile file,
            String folderName,
            String originalFileName,
            String resourceType) {
        var options = ObjectUtils.asMap(
                "folder", "unraveldocs/" + folderName,
                "public_id", generateRandomPublicId(originalFileName),
                "overwrite", true,
                "resource_type", resourceType);
        try {
            var uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    // generate random public ID for file upload
    public String generateRandomPublicId(String originalFileName) {
        return UUID.randomUUID() + "-" + originalFileName.replaceAll("[^a-zA-Z0-9]", "_");
    }
}
