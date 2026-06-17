package com.cloudstorage.model.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageServiceImpl implements StorageService{
    private final String UPLOAD_DIR = "C:\\Users\\arvin\\OneDrive - Graphic Era University\\Desktop\\Cloud Based Storage Service\\files\\";

    public LocalStorageServiceImpl() {
        //prior starting springBoot first check if directory exists already
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String uniqueFileName) {
        try {
            //build the exact location where file lives
            Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);

            //copy the bytes coming from the incoming web requests to local Drive
            Files.copy(file.getInputStream(), filePath,StandardCopyOption.REPLACE_EXISTING);

            //return the file path -> save in PostGres
            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed To Store File Locally: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String storageKey) {
        try {
            return new FileInputStream(storageKey);
        } catch (Exception e) {
            throw new RuntimeException("File Not Found in LocalDisk: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storageKey) {
        File file = new File(storageKey);
        if (file.exists()) {
            file.delete();
        }
        
    }
}
