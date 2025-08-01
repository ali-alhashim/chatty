package com.chatty.service;

import com.chatty.config.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.uploads-base-dir}")
    private String uploadsBaseDir;

    public String storeAvatar(MultipartFile avatarFile, String userId) throws FileStorageException {
        // Validate input parameters
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new FileStorageException("Cannot store empty file.");
        }

        if (userId == null || userId.trim().isEmpty()) {
            throw new FileStorageException("User ID cannot be null or empty.");
        }

        // Validate content type
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("Only image files are allowed.");
        }

        // Validate file size (5MB limit)
        if (avatarFile.getSize() > 5 * 1024 * 1024) {
            throw new FileStorageException("Image must be less than 5 MB.");
        }

        try {
            // Create base upload directory path and normalize it
            Path baseDir = Path.of(uploadsBaseDir).toAbsolutePath().normalize();

            // Create user-specific avatar directory
            Path userAvatarDir = baseDir.resolve(userId).resolve("avatar").normalize();

            // Security check: ensure the directory is within the base directory
            if (!userAvatarDir.startsWith(baseDir)) {
                throw new FileStorageException("Invalid user ID - path traversal detected.");
            }

            // Create directories if they don't exist
            Files.createDirectories(userAvatarDir);

            // Generate unique filename with original extension
            String originalFilename = avatarFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Create the final file path
            Path avatarPath = userAvatarDir.resolve(uniqueFileName).normalize();

            // Final security check: ensure the file path is within the base directory
            if (!avatarPath.startsWith(baseDir)) {
                throw new FileStorageException("Invalid file path detected - security violation.");
            }

            // Transfer the file
            avatarFile.transferTo(avatarPath.toFile());

            // Return the relative path from the base directory
            Path relativePath = baseDir.relativize(avatarPath);
            return relativePath.toString().replace("\\", "/");

        } catch (IOException e) {
            throw new FileStorageException("Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to delete an existing avatar file
     */
    public boolean deleteAvatar(String avatarPath) {
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            return false;
        }

        try {
            Path baseDir = Path.of(uploadsBaseDir).toAbsolutePath().normalize();
            Path fileToDelete = baseDir.resolve(avatarPath).normalize();

            // Security check
            if (!fileToDelete.startsWith(baseDir)) {
                return false;
            }

            return Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            return false;
        }
    }
}