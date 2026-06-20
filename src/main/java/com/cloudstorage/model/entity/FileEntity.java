package com.cloudstorage.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "files") //No restriction on the database. ALways name you table name as real world
@SQLDelete(sql = "UPDATE files SET is_trash = true WHERE id =?") //softdelete
@SQLRestriction("is_trash = false")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false,nullable = false,length = 36)
    private String id;

    @Column(nullable = false,updatable = true)
    private String originalName;

    @Column(nullable = false,updatable = true)
    private String storageKey;  //acutal path

    //how to fetch for entire files
    //Don't need BigInt -> long is enough
    @Column(nullable = false,updatable = true)
    private Long sizeInBytes;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "folder_Id")
    private Folder folder;

    @Column(name = "is_trash",nullable = false)
    private boolean isTrash;

    @Column(nullable = false)
    private String mimeType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(updatable = true)
    private LocalDateTime updatedAt;
}
