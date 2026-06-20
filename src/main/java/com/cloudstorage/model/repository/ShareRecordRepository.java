package com.cloudstorage.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudstorage.model.entity.ShareRecord;

public interface ShareRecordRepository extends JpaRepository<ShareRecord, String>{
    
    // A. Internal Sharing
    
    // 1.Check if a specific User has Access to specific File
    Optional<ShareRecord> findByFileIdAndSharedWithUserId(String fileId, String sharedWithUserId);

    // 2.Check if a specific User has Access to specific Folder
    Optional<ShareRecord> findByFolderIdAndSharedWithUserId(String folderId, String sharedWithUserId);
    
    // B. Public Anonymous Link
    Optional<ShareRecord> findByPublicToken(String publicToken);

    // C.User Management Only
    // 4. Let the Owner see everyone they invited to a specific file
    List<ShareRecord> findAllByFileIdAndOwnerId(String fileId, String ownerId);

    // 5. Let the Owner see everyone they invited to a specific folder
    List<ShareRecord> findAllByFolderIdAndOwnerId(String folderId, String ownerId);

    // 6. Find all files shared WITH a specific user (For a "Shared with me"
    // dashboard)
    List<ShareRecord> findAllBySharedWithUserId(String sharedWithUserId);
}
