package com.example.SpringBoot_Bakend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.url:}") String cloudinaryUrl,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret
    ) {
        if (cloudinaryUrl != null && !cloudinaryUrl.trim().isEmpty()) {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
            log.info("Cloudinary initialized using CLOUDINARY_URL.");
        } else if (cloudName != null && !cloudName.trim().isEmpty() &&
                   apiKey != null && !apiKey.trim().isEmpty() &&
                   apiSecret != null && !apiSecret.trim().isEmpty()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            log.info("Cloudinary initialized using cloud-name: {}", cloudName);
        } else {
            String envUrl = System.getenv("CLOUDINARY_URL");
            if (envUrl != null && !envUrl.trim().isEmpty()) {
                this.cloudinary = new Cloudinary(envUrl);
                log.info("Cloudinary initialized using system env CLOUDINARY_URL.");
            } else {
                this.cloudinary = new Cloudinary(ObjectUtils.emptyMap());
                log.warn("Cloudinary credentials not provided. Avatar uploads will require CLOUDINARY_URL.");
            }
        }
    }

    public String uploadAvatar(MultipartFile file, String userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "smritisetu/avatars",
                "public_id", "avatar_" + userId + "_" + System.currentTimeMillis(),
                "overwrite", true,
                "resource_type", "image"
        ));

        Object secureUrl = uploadResult.get("secure_url");
        if (secureUrl != null) {
            return secureUrl.toString();
        }
        Object url = uploadResult.get("url");
        return url != null ? url.toString() : null;
    }
}
