package com.cloudstorage.model.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.entity.Folder;
import com.cloudstorage.model.entity.Users;
import com.cloudstorage.model.repository.FileRepository;
import com.cloudstorage.model.repository.FolderRepository;
import com.cloudstorage.model.repository.UserRepository;

import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;

    public FileEntity uploadLocalFile(MultipartFile file, String folderId){
        //Identify who is holding the VIP Badge
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Users currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in DB"));

        //Generate Collision Proof file name
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + originalFileName;

        //Hand RawFile to upload(Storage) Engine-> Local Drive
        String physicalStoragePath =  storageService.uploadFile(file, uniqueFileName);

        //Build Metadata for FileEntity Database storage
        FileEntity fileMetaData = new FileEntity();
        fileMetaData.setOriginalName(originalFileName);
        fileMetaData.setStorageKey(physicalStoragePath); //path for physical file
        fileMetaData.setSizeInBytes(file.getSize());
        fileMetaData.setMimeType(file.getContentType()); //ex png/pdf
        fileMetaData.setTrash(false);
        fileMetaData.setUser(currentUser);
        
        //Handle Folder Logic
        if (folderId != null && !folderId.trim().trim().isEmpty()) {
            //Find the Folder and security reach
            Folder targetFolder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Target folder not found."));

            // SECURITY GATE: Does the user own this folder?
            if (!targetFolder.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("Security Breach: Cannot upload to someone else's folder.");
            }
            //set the file to folder
            fileMetaData.setFolder(targetFolder);
        }
        return fileRepository.save(fileMetaData);
    }

    // 1. Rename a File
    public FileEntity renameFile(String fileId, String newName) {
        // Fetch securely (will throw error if they don't own it)
        FileEntity file = getFileMetadataSecurely(fileId);

        file.setOriginalName(newName);
        return fileRepository.save(file);
    }

    // 2. Move a File to a different folder
    public FileEntity moveFile(String fileId, String targetFolderId) {
        FileEntity file = getFileMetadataSecurely(fileId);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // If no target folder is provided, move it back to the "Home" root directory
        if (targetFolderId == null || targetFolderId.trim().isEmpty()) {
            file.setFolder(null);
        } else {
            // Find the new folder and check ownership
            Folder targetFolder = folderRepository.findById(targetFolderId)
                    .orElseThrow(() -> new RuntimeException("Target folder not found."));

            if (!targetFolder.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("Security Breach: Cannot move file to a folder you don't own.");
            }

            file.setFolder(targetFolder);
        }

        return fileRepository.save(file);
    }

    // Checking the Request validity by Checking authority
    public FileEntity getFileMetadataSecurely(@NonNull String fileId) {
        // 1. Who is asking?
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Does the file exist?
        FileEntity fileMetadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found in database."));

        // 3. THE SECURITY GATE: Do they own it? (Later we will add "Shared" logic here)
        if (!fileMetadata.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Security Breach: You do not have permission to access this file.");
        }

        return fileMetadata;
    }

    @Transactional
    public void emptyFileTrash(){
        // 1. Identify the user
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Users currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 2.Fetch all Ghost files into java Memory
        List<FileEntity> trashedFiles = fileRepository.findAllTrashedFilesByUser(currentUser.getId());

        //If its empty return
        if (trashedFiles.isEmpty()) {
            return;
        } 

        // 3.Physical Purge Phase -> hard deleting before record update
        for(FileEntity file:trashedFiles){
            try {
                //Delete Physically From Local or Bucket
                storageService.deleteFile(file.getStorageKey());
            } catch (Exception e) {
                System.err.println("Warning: Could Not Delete File Physically: " + file.getOriginalName());
            }
        } 

        //4.Database Purge
        fileRepository.permanentlyDeleteTrashedFilesByUser(currentUser.getId());
    }


    public void moveToTrash(String fileId){
        FileEntity fileMetaData = getFileMetadataSecurely(fileId);
        
        //2.Perform Soft Delete 
        fileRepository.delete(fileMetaData); //Not calling Service class to implement to soft delete feature
    }
}
