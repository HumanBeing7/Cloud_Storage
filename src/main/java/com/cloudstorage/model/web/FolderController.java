package com.cloudstorage.model.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cloudstorage.model.entity.Folder;
import com.cloudstorage.model.service.FolderService;

import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<?> createFolder(@RequestBody Map<String, String> requestData) {
        try {
            String name = requestData.get("name");
            String parentId = requestData.get("parentId"); // This will be null for Root folders

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Folder name is required.");
            }

            Folder createdFolder = folderService.createFolder(name, parentId);

            return ResponseEntity.ok(
                    "Folder '" + createdFolder.getName() + "' created successfully with ID: " + createdFolder.getId());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create folder: " + e.getMessage());
        }
    }
}