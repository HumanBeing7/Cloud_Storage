package com.cloudstorage.model.web;

import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.enums.PermissionLevel;
import com.cloudstorage.model.service.ShareService;
import com.cloudstorage.model.service.StorageService;

import lombok.AllArgsConstructor;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.io.InputStream;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/shares")
public class ShareController {
    private final ShareService shareService;
    private final StorageService storageService;

    // ROUTE A: INTERNAL SECURE SHARING
    @PostMapping("/file/{fileId}/invite")
    public ResponseEntity<?> inviteUserToFile(
            @PathVariable String fileId,
            @RequestParam String recipientEmail,
            @RequestParam PermissionLevel permission) {
        try {
            shareService.inviteUserToFile(fileId, recipientEmail, permission);
            return ResponseEntity.ok("Successfully granted " + permission + " access to " + recipientEmail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to share file: " + e.getMessage());
        
        }
    }

    // ROUTE B: PUBLIC ANONYMOUS LINKS
    @PostMapping("/file/{fileId}/link")
    public ResponseEntity<?> generatePublicLink(
            @PathVariable String fileId,
            @RequestParam PermissionLevel permission,
            @RequestParam(required = false) Integer daysUntilExpiry) {
        try {
            String token = shareService.generatePublicLinkForFile(fileId, permission, daysUntilExpiry);

            // Construct a usable URL that the user can copy-paste!
            String publicUrl = "http://localhost:8081/api/shares/shared/" + token;

            return ResponseEntity.ok(Map.of(
                    "message", "Public link generated successfully!",
                    "url", publicUrl,
                    "token", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate link: " + e.getMessage());
        }
    }

    // CONSUMING THE PUBLIC LINK (NO LOGIN REQUIRED)
    @GetMapping("/shared/{token}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String token) {
        try {
            // 1. Validate the token and get the file metadata
            FileEntity file = shareService.getFileFromPublicToken(token);

            // 2. Grab the physical file from the hard drive
            Resource resource = new FileSystemResource(file.getStorageKey());

            // 3. Stream it to the user's browser
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getOriginalName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}