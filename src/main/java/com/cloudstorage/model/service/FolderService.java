package com.cloudstorage.model.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cloudstorage.model.entity.Folder;
import com.cloudstorage.model.entity.Users;
import com.cloudstorage.model.repository.FolderRepository;
import com.cloudstorage.model.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    
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
}
