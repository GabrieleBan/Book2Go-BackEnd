package com.b2g.readerservice.controller;

import com.b2g.readerservice.annotation.RequireRole;
import com.b2g.readerservice.service.MinioStorageService;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/content")
@RequiredArgsConstructor

public class ContentMockedServiceController {

    private final MinioStorageService storageService;

    @PostMapping("/{bookId}/cover-image")
    @RequireRole("ADMIN")
    public ResponseEntity<String> addCoverImage(
            @PathVariable UUID bookId,
            @RequestParam("file") MultipartFile file) throws Exception {

        String filename = storageService.uploadFile(bookId, file);
        return ResponseEntity.ok("Salvata immagine: " + filename);
    }

    @GetMapping("/{bookId}/cover-image")
    public ResponseEntity<byte[]> getCoverImage(@PathVariable UUID bookId) {
        try {
            byte[] data = storageService.downloadFile(bookId);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // ora è sempre jpg
                    .body(data);

        } catch (ErrorResponseException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
