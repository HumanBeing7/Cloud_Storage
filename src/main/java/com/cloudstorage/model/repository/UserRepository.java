package com.cloudstorage.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cloudstorage.model.entity.Users;

public interface UserRepository extends JpaRepository<Users,String>{
    //check if the users exist by email? true:false
    Optional<Users> findByEmail(String email); //get credential for the users as per the email
    boolean existsByEmail(String email);  //to check if the email is already taken
}
