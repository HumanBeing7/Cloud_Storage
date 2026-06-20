package com.cloudstorage.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.cloudstorage.model.enums.PermissionLevel;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "share_records")
public class ShareRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36,updatable = false,nullable = false)
    private String id;

    //Targets
    @ManyToOne(fetch = FetchType.LAZY) // To load only when needed ffor files
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY) // To load only when needed for folders
    @JoinColumn(name = "folder_id")
    private Folder folder;      //populated only when sharing entire folder tree

    //The Acutal User Who holds Files and Folders
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Users owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id")
    private Users sharedWithUser; // The user with whom the file or folder is shared

    //Public Link Key 
    @Column(name = "public_token", unique = true)
    private String publicToken; //Random Unguessable String(Null if Invited People Only)

    //Permission Level
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionLevel permission;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Optional expiration date for the share

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
