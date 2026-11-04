package za.co.gymhubsa.api.common.storage;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves previously uploaded files (profile photos, receipts, etc.) by the
 * relative path returned from {@link FileStorageService#store}.
 */
@Tag(name = "Files", description = "Download previously uploaded files")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{subdirectory}/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String subdirectory, @PathVariable String filename) {
        Resource resource = fileStorageService.load(subdirectory + "/" + filename);
        return ResponseEntity.ok().body(resource);
    }
}
