package com.cloudstorage.model.service;

import java.io.InputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    //Takse the Raw file and returns the unique storage key/path
    String uploadFile(MultipartFile file, String uniqueFileName);

    //Retrieve file bytes for download 
    InputStream downloadFile(String storageKey);

    //delete file from hardDrive/ cloud
    void deleteFile(String storageKey);
}
