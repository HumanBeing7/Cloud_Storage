package com.cloudstorage.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.entity.Folder;
import com.cloudstorage.model.entity.Users;

public interface FileRepository extends JpaRepository<FileEntity,String>{
    //List of Files where Folder ->Null
    //HomeScreen
    List<FileEntity> findByUserAndFolderIsNull(Users user);
     
    //For Folder
    List<FileEntity> findByFolder(Folder folder);
}
