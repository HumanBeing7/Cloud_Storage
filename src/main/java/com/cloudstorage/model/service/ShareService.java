package com.cloudstorage.model.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.entity.ShareRecord;
import com.cloudstorage.model.entity.Users;
import com.cloudstorage.model.enums.PermissionLevel;
import com.cloudstorage.model.repository.FileRepository;
import com.cloudstorage.model.repository.ShareRecordRepository;
import com.cloudstorage.model.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ShareService {
    private final ShareRecordRepository shareRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShareRecord inviteUserToFile(String fileId, String recipientEmail, PermissionLevel permission) {
        Users currentUser = getCurrentUser();
        FileEntity file = getFileAndVerifyOwnership(fileId, currentUser);

        // 1. Find the user they want to invite
        Users recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new RuntimeException("User with email " + recipientEmail + " does not exist on this platform."));

        // 2. Prevent sharing with yourself
        if (currentUser.getId().equals(recipient.getId())) {
            throw new RuntimeException("You already own this file.");
        }

        // 3. Check if we already shared this file with them (Upsert logic)
        Optional<ShareRecord> existingShare = shareRepository.findByFileIdAndSharedWithUserId(file.getId(), recipient.getId());
        
        ShareRecord shareRecord;
        if (existingShare.isPresent()) {
            // Update existing permission (e.g., upgrading them from VIEWER to EDITOR)
            shareRecord = existingShare.get();
            shareRecord.setPermission(permission);
        } else {
            // Create brand new permission
            shareRecord = new ShareRecord();
            shareRecord.setFile(file);
            shareRecord.setOwner(currentUser);
            shareRecord.setSharedWithUser(recipient);
            shareRecord.setPermission(permission);
        }

        return shareRepository.save(shareRecord);
    }

    // B. Public Anonymous Link
    @Transactional
    public String generatePublicLinkForFile(String fileId, PermissionLevel permission, Integer daysUntilExpiry) {
        Users currentUser = getCurrentUser();
        FileEntity file = getFileAndVerifyOwnership(fileId, currentUser);

        // 1. Generate an unguessable token
        String publicToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        // 2. Create the anonymous share record
        ShareRecord shareRecord = new ShareRecord();
        shareRecord.setFile(file);
        shareRecord.setOwner(currentUser);
        shareRecord.setPublicToken(publicToken);
        shareRecord.setPermission(permission);

        // 3. Handle optional expiration
        if (daysUntilExpiry != null && daysUntilExpiry > 0) {
            shareRecord.setExpiresAt(LocalDateTime.now().plusDays(daysUntilExpiry));
        }

        shareRepository.save(shareRecord);

        // Return the token so the Controller can format it into a full URL (e.g., http://localhost:8081/api/share/...)
        return publicToken;
    }

    @Transactional(readOnly = true)
    public FileEntity getFileFromPublicToken(String publicToken) {
        // 1. Find the token in the database
        ShareRecord shareRecord = shareRepository.findByPublicToken(publicToken)
                .orElseThrow(() -> new RuntimeException("Invalid or missing public link."));

        // 2. Check for expiration
        if (shareRecord.getExpiresAt() != null && shareRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Optional: You could actually delete the ShareRecord here to clean up the DB!
            throw new RuntimeException("This shared link has expired.");
        }

        // 3. Make sure the file isn't in the trash
        FileEntity file = shareRecord.getFile();
        if (file == null || file.isTrash()) {
            throw new RuntimeException("The shared file no longer exists or was moved to the trash.");
        }

        return file;
    }

    // Security Line
    private Users getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }

    // File Threft Protection
    private FileEntity getFileAndVerifyOwnership(String fileId, Users currentUser) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found."));
        if (!file.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Security Breach: You cannot share a file you do not own.");
        }
        return file;
    }

}
