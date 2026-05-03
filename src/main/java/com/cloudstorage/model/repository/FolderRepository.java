package com.cloudstorage.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudstorage.model.entity.Folder;
import com.cloudstorage.model.entity.Users;

public interface FolderRepository extends JpaRepository<Folder,String>{
    //1.don't require Optional saftey check cuz if Not Found -> empty list
    //2.We need to fetch only the root folder for the user ID
    //acutal Query 
    //SELECT * FROM folders 
    //WHERE user_id = ?
    //AND parent_id
    //IS NULL
    //AND is_trash = false;
    //3.parentFolder -> ParentFolder
    List<Folder> findByUserAndParentFolderIsNull(Users user);
}
