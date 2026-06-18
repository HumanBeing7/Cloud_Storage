package com.cloudstorage.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cloudstorage.model.entity.FileEntity;
import com.cloudstorage.model.entity.Folder;
import com.cloudstorage.model.entity.Users;

public interface FileRepository extends JpaRepository<FileEntity,String>{
    //List of Files where Folder ->Null
    //HomeScreen
    List<FileEntity> findByUserAndFolderIsNull(Users user);
     
    //For Folder
    List<FileEntity> findByFolder(Folder folder);

    //1.Retrieval: Fetch all Ghost Files for a user
    @Query(value = "SELECT * FROM files WHERE is_trash = true AND user_id =:userId", nativeQuery = true)
    List<FileEntity> findAllTrashedFilesByUser(@Param("userId") String userId);

    //2.The Purge: Permanently Delete Ghost Files from Database For Specific User
    @Modifying //required for INSERT, UPDATE and DELETE
    @Query(value = "DELETE FROM files WHERE is_trash = true AND user_id = :userId", nativeQuery = true)
    void permanentlyDeleteTrashedFilesByUser(@Param("userId") String userId);
}
