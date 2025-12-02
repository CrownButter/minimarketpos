package com.pos.minimarketpos.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class FileUploadUtil {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Upload a file to the server
     */
    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        // Validate file
        validateFile(file);

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Copy file to target location
        Path targetLocation = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("File uploaded successfully: {}", uniqueFilename);

        return subDirectory + "/" + uniqueFilename;
    }

    /**
     * Upload and resize an image
     */
    public String uploadImage(MultipartFile file, String subDirectory, int maxWidth, int maxHeight)
            throws IOException {

        validateImageFile(file);

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Read and resize image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        BufferedImage resizedImage = resizeImage(originalImage, maxWidth, maxHeight);

        // Save resized image
        Path targetLocation = uploadPath.resolve(uniqueFilename);
        String formatName = fileExtension.substring(1); // Remove the dot
        ImageIO.write(resizedImage, formatName, targetLocation.toFile());

        log.info("Image uploaded and resized successfully: {}", uniqueFilename);

        return subDirectory + "/" + uniqueFilename;
    }

    /**
     * Upload product image with thumbnail
     */
    public String[] uploadProductImage(MultipartFile file) throws IOException {
        validateImageFile(file);

        String mainImage = uploadImage(file, "products", 800, 800);

        // Create thumbnail
        Path uploadPath = Paths.get(uploadDir, "products/thumbnails");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        BufferedImage thumbnail = resizeImage(originalImage, 200, 200);

        Path thumbnailLocation = uploadPath.resolve(uniqueFilename);
        String formatName = fileExtension.substring(1);
        ImageIO.write(thumbnail, formatName, thumbnailLocation.toFile());

        String thumbnailPath = "products/thumbnails/" + uniqueFilename;

        log.info("Product image and thumbnail uploaded successfully");

        return new String[]{mainImage, thumbnailPath};
    }

    /**
     * Delete a file
     */
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        Files.deleteIfExists(path);
        log.info("File deleted successfully: {}", filePath);
    }

    /**
     * Validate file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.contains("..")) {
            throw new IllegalArgumentException("Invalid file path: " + filename);
        }
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: " + ALLOWED_IMAGE_TYPES);
        }
    }

    /**
     * Validate document file
     */
    public void validateDocumentFile(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        if (!ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: " + ALLOWED_DOCUMENT_TYPES);
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) return "";
        return filename.substring(lastDotIndex);
    }

    /**
     * Resize an image maintaining aspect ratio
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth = maxWidth;
        int newHeight = (int) (maxWidth / aspectRatio);

        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (int) (maxHeight * aspectRatio);
        }

        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();

        // Set rendering hints for better quality
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * Get file path for serving
     */
    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename).normalize();
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        Path path = Paths.get(uploadDir, filePath);
        return Files.exists(path);
    }

    /**
     * Get file size
     */
    public long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        return Files.size(path);
    }
}