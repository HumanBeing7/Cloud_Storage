package com.cloudstorage.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudstorage.model.dto.folderDTO.DriveContentDTO;
import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.entity.Folder;
import com.cloudstorage.model.entity.Users;
import com.cloudstorage.model.repository.FileRepository;
import com.cloudstorage.model.repository.FolderRepository;
import com.cloudstorage.model.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final StorageService storageService;
    
    public Folder createFolder(String folderName, String parentId){
        //1.Identify the Secure user
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Users currUser = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Authenticated User Not Found"));

        //2.Build New Folder
        Folder newFolder = new Folder();
        newFolder.setName(folderName);
        newFolder.setUser(currUser);
        newFolder.setTrash(false);

        //3.Handle Nesting of the User is valid
        if(parentId != null && !parentId.trim().isEmpty()){
            Folder parentFolder = folderRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent Folder Doesn't Exist"));

            //Security Breach -> Folder OwnerShip
            if (!parentFolder.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("Security Breach: You Cannot Make Folder In Else's Folder");
            }
            newFolder.setParentFolder(parentFolder);
        }
        //4.Save to PostGres
        return folderRepository.save(newFolder);
    }

    //Get Root contents in drive
    public DriveContentDTO getRootContents(){
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Users currUser = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User Not Found!"));
        
        //Fetch all folder where parent = null
        List<Folder> rootFolders = folderRepository.findByUserAndParentFolderIsNull(currUser);

        //Same for File
        List<FileEntity> rootFiles = fileRepository.findByUserAndFolderIsNull(currUser);

        //give these to DTO
        //Helper Method to map entites -> same DTO
        return entityToDTO(rootFolders, rootFiles);
    }

    public DriveContentDTO getFolderContents(String folderId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Folder currentFolder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // Security check
        if (!currentFolder.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Security Breach: Unauthorized access.");
        }

        // The Folder entity already contains its children because of your @OneToMany
        // mappings!
        List<Folder> subFolders = currentFolder.getChildFolders();
        List<FileEntity> files = currentFolder.getFileEntities();

        return entityToDTO(subFolders, files);
    }

    // Recursively Call to fetch all physical location of the files and sub folder inside a folder 
    private void collectAllPhysicalFilePaths(Folder currentFolder, List<String> pathsToDestroy) {
        //Base Case 
        if (currentFolder == null) {
            return; // Stop right here and go back up the tree!
        }
        // 1. Harvest files directly inside this folder
        if (currentFolder.getFileEntities() != null) {
            for (FileEntity file : currentFolder.getFileEntities()) {
                pathsToDestroy.add(file.getStorageKey());
            }
        }
        
        // 2. The Recursion: Dive into every sub-folder and repeat!
        if (currentFolder.getChildFolders() != null) {
            for (Folder subFolder : currentFolder.getChildFolders()) {
                collectAllPhysicalFilePaths(subFolder, pathsToDestroy);
            }
        }
    }

    // Move a folder to the trash
    public void moveFolderToTrash(String folderId) {
        // 1. Identify the secure user
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the folder
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found."));

        // 3. Security Gate
        if (!folder.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Security Breach: You cannot delete someone else's folder.");
        }

        // 4. Trigger the Soft Delete
        // Hibernate will intercept this and run: UPDATE folders SET is_trash = true
        // It will then cascade to all child files and folders and do the exact same
        // thing!
        folderRepository.delete(folder);
    }

    @Transactional
    public void emptyFolderTrash(){
        // 1. Identify the user
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Users currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 2. Fetch Top level trashed Folder
        List<Folder> trashedFolders = folderRepository.findAllTrashedFoldersByUser(currentUser.getId());

        if (trashedFolders.isEmpty()) {
            return;
        }

        List<String> physicalPathsToDestroy = new ArrayList<>();

        //3. Unleash Recursive Harverster on every Folder ->. to get all physical Paths
        for(Folder trashedFolder:trashedFolders){
            collectAllPhysicalFilePaths(trashedFolder, physicalPathsToDestroy);
        }

        //4. Physical Purge -> DESTROY
        for(String path:physicalPathsToDestroy){
            try{
                storageService.deleteFile(path);
            }catch(Exception e) {
                System.err.println("Warning: Could not delete physical file: " + path);
            }
        }

        //5. Database Purge: Spring Hibernate Would Automatically Handle the Cascade Deletion 
        // We completely bypass Hibernate and execute Native SQL to avoid "null entity"
        // crashes.

        // First: Wipe the orphaned file metadata to prevent Foreign Key constraint
        // errors
        fileRepository.permanentlyDeleteTrashedFilesByUser(currentUser.getId());

        // Second: Wipe the trashed folders
        folderRepository.permanentlyDeleteTrashedFoldersByUser(currentUser.getId());
    }

    private DriveContentDTO entityToDTO(List<Folder> folders, List<FileEntity> files){
        List<DriveContentDTO.FolderSummary> folderSummaries = folders.stream()
                .filter(f -> !f.isTrash()) // Don't show trashed folders
                .map(f -> new DriveContentDTO.FolderSummary(f.getId(), f.getName()))
                .collect(Collectors.toList());

        List<DriveContentDTO.FileSummary> fileSummaries = files.stream()
                .filter(f -> !f.isTrash()) // Don't show trashed files
                .map(f -> new DriveContentDTO.FileSummary(f.getId(), f.getOriginalName(), f.getSizeInBytes(), f.getMimeType()))
                .collect(Collectors.toList());

        return new DriveContentDTO(folderSummaries, fileSummaries);
    }
}
