package com.cloudstorage.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.CascadeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "folders")
@SQLDelete(sql = "UPDATE folders SET is_trash = true WHERE id =?")
@SQLRestriction("is_trash = false")
@NoArgsConstructor
@Getter
@Setter
public class Folder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) //primary Key
    @Column(name = "id",nullable = false,updatable = false,length = 36)
    private String id;

    @Column(nullable = false,updatable = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    //explicitely mentioning the name here cux SQLRestriciton is using it
    @Column(name = "is_trash",nullable = false)
    private boolean isTrash;

    //self_referencing
    @ManyToOne //owning side
    @JoinColumn(name = "parent_Id")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> childFolders;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    //we want to remove all the files related to a folder
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> fileEntities;
}
