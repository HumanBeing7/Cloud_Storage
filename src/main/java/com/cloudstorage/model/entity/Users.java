package com.cloudstorage.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.cloudstorage.model.config.AppRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "users")
@Entity

public class Users {
    @Id  //primary key
    @GeneratedValue(strategy = GenerationType.UUID)   //unique 36 character id
    @Column(updatable = false, nullable = false, length = 36)  //ensure any force update, nullable, with fixed length -> cuz hibernate tell db to create varchar(255)
    private String id;

    @Email
    @Column(nullable = false,unique = true)
    @NotNull
    private String email;

    //Cannot be null, need not to be uniquw
    @NotNull
    @Column(nullable = false)
    private String password;

    //cannot be null and must be enummarated (If the enumerated type is not )
    // specified or the Enumerated annotation is not used, the EnumType value is
    // assumed to be ORDINAL)
    @Enumerated(EnumType.STRING)
    private AppRole role;


    //Time createdAt;
    @CreationTimestamp   
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    //@Column(updatable = true) not need now cuz by default every field in updatable by nature 
    private LocalDateTime updatedAt;
    //Time updatedAt;

    //one user -> multiple Folde
    //1.mappedBy,2. Cascade feature, 3.Orphan removal 4.softdelete feature

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Folder> folders;


    //remove orphan if it gets deleted
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<FileEntity> fileEntities;
}
