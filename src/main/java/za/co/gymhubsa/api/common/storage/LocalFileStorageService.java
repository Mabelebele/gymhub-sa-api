package za.co.gymhubsa.api.common.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Stores uploaded files on the local filesystem under {@code app.file-storage.base-dir}.
 * Suitable for local dev/testing; replace with a cloud-backed implementation
 * for production without changing any calling code.
 */
@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageProperties properties;

    @Override
    public String store(MultipartFile file, String subdirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file must not be empty");
        }

        try {
            Path targetDir = baseDir().resolve(subdirectory).normalize();
            Files.createDirectories(targetDir);

            String extension = extensionOf(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path targetFile = targetDir.resolve(filename);

            Files.copy(file.getInputStream(), targetFile);

            return subdirectory + "/" + filename;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store uploaded file: " + ex.getMessage());
        }
    }

    @Override
    public Resource load(String relativePath) {
        try {
            Path file = baseDir().resolve(relativePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + relativePath);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + relativePath);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(baseDir().resolve(relativePath).normalize());
        } catch (IOException ex) {
            // Best-effort cleanup; do not fail the calling operation over a stale file.
        }
    }

    private Path baseDir() {
        return Paths.get(properties.baseDir()).toAbsolutePath();
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex >= 0 ? originalFilename.substring(dotIndex) : "";
    }
}
