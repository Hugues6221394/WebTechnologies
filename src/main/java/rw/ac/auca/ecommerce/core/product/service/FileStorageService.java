package rw.ac.auca.ecommerce.core.product.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = System.getProperty("user.home") + File.separator + "ecommerce_uploads" + File.separator;

    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        // Generate unique file name
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Create upload directory if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Save file to absolute path
        File savedFile = new File(uploadDir + fileName);
        file.transferTo(savedFile);

        // Return path relative to frontend access (optional for preview)
        return "/images/" + fileName; // Optional mapping
    }
}
