package com.cloudstorage.model.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cloudstorage.model.dto.folderDTO.DriveContentDTO;
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

    @GetMapping
    public ResponseEntity<DriveContentDTO> getRootFolder() {
        return ResponseEntity.ok(folderService.getRootContents());
    }
    
    // 2. Get specific Folder contents
    @GetMapping("/{id}")
    public ResponseEntity<DriveContentDTO> getFolderContents(@PathVariable String id) {
        return ResponseEntity.ok(folderService.getFolderContents(id));
    }

    // Move folder to trash
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(@PathVariable String id) {
        try {
            folderService.moveFolderToTrash(id);
            return ResponseEntity.ok("Folder and all its contents successfully moved to the trash.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to move folder to trash: " + e.getMessage());
        }
    }

    @DeleteMapping("/trash/empty")
    public ResponseEntity<?> emptyFolderTrash() {
        try {
            folderService.emptyFolderTrash();
            return ResponseEntity.ok("Folder trash emptied. All nested files and database records destroyed.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to empty folder trash: " + e.getMessage());
        }
    }
}