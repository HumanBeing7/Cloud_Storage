package com.cloudstorage.model.dto.folderDTO;

import java.util.List;

public record DriveContentDTO(
        List<FolderSummary> folders,
        List<FileSummary> files) {
    public record FolderSummary(String id, String name) {
    }

    public record FileSummary(String id, String name, Long sizeInBytes, String mimeType) {
    }
}