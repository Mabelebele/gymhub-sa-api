package za.co.gymhubsa.api.common.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over where uploaded files (profile photos, receipts, generated
 * reports) are persisted. {@link LocalFileStorageService} is the dev/MVP
 * implementation; a cloud-backed implementation (e.g. S3/Azure Blob) can be
 * swapped in later without touching callers.
 */
public interface FileStorageService {

    /**
     * Stores the given file under a logical subdirectory (e.g. "members",
     * "receipts") and returns a relative path that can later be resolved via
     * {@link #load(String)}.
     */
    String store(MultipartFile file, String subdirectory);

    Resource load(String relativePath);

    void delete(String relativePath);
}
