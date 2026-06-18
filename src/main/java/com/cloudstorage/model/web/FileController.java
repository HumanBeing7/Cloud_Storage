package com.cloudstorage.model.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.service.FileService;
import com.cloudstorage.model.service.StorageService;

import lombok.AllArgsConstructor;

import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;
    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) String folderId) {
        try {
            // Check if the user forgot to attach a file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }

            FileEntity savedFile = fileService.uploadLocalFile(file,folderId);

            // Return success with the new Database ID!
            return ResponseEntity.ok("File uploaded successfully! Database ID: " + savedFile.getId());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable String id) {
        try {
            // 1. Run the Security Check and get the metadata
            FileEntity fileMetadata = fileService.getFileMetadataSecurely(id);

            // 2. Ask the Storage Engine for the raw bytes from the hard drive
            InputStream fileStream = storageService.downloadFile(fileMetadata.getStorageKey());
            InputStreamResource resource = new InputStreamResource(fileStream);

            // 3. Package it up with standard HTTP Download Headers
            return ResponseEntity.ok()
                    // Tell the browser "This is an attachment, download it with this exact name"
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getOriginalName() + "\"")
                    // Tell the browser what kind of file it is (PDF, Image, etc.)
                    .contentType(MediaType.parseMediaType(fileMetadata.getMimeType()))
                    .contentLength(fileMetadata.getSizeInBytes())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Download failed: " + e.getMessage());
        }
    }

    // 1. Rename Endpoint
    @PutMapping("/{id}/rename")
    public ResponseEntity<?> renameFile(
            @PathVariable String id, 
            @RequestParam("newName") String newName) {
        try {
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("New name cannot be empty.");
            }
            FileEntity updatedFile = fileService.renameFile(id, newName);
            return ResponseEntity.ok("File renamed successfully to: " + updatedFile.getOriginalName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Rename failed: " + e.getMessage());
        }
    }

    // 2. Move Endpoint
    @PutMapping("/{id}/move")
    public ResponseEntity<?> moveFile(
            @PathVariable String id, 
            @RequestParam(value = "folderId", required = false) String folderId) {
        try {
            fileService.moveFile(id, folderId);
            return ResponseEntity.ok("File moved successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Move failed: " + e.getMessage());
        }
    }

    // Add this to FileController.java
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id) {
        try {
            fileService.moveToTrash(id);
            return ResponseEntity.ok("File moved to trash successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete file: " + e.getMessage());
        }
    }

    // Add this endpoint
    @DeleteMapping("/trash/empty")
    public ResponseEntity<?> emptyFileTrash() {
        try {
            fileService.emptyFileTrash();
            return ResponseEntity.ok("Trash emptied successfully. All physical files and database records destroyed.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to empty trash: " + e.getMessage());
        }
    }
    
}
