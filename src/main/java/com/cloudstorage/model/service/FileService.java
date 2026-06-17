package com.cloudstorage.model.service;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.entity.Users;
import com.cloudstorage.model.repository.FileRepository;
import com.cloudstorage.model.repository.UserRepository;

import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;

    public FileEntity uploadLocalFile(MultipartFile file){
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
        
        //LEAVE FOLDER NULL -> FIRST FILE SITS AT ROOT 
         return fileRepository.save(fileMetaData);
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

    public void moveToTrash(String fileId){
        FileEntity fileMetaData = getFileMetadataSecurely(fileId);
        
        //2.Perform Soft Delete 
        fileRepository.delete(fileMetaData); //Not calling Service class to implement to soft delete feature
    }
}
