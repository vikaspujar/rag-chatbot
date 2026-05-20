package com.rag.chatbot.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DocumentInfo {
    private String id;
    private String fileName;
    private String fileType;
    private int chunkCount;
    private String uploadedAt;
    private long sizeBytes;

    public DocumentInfo(String id, String fileName, String fileType, int chunkCount, long sizeBytes) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.chunkCount = chunkCount;
        this.sizeBytes = sizeBytes;
        this.uploadedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public int getChunkCount() { return chunkCount; }
    public String getUploadedAt() { return uploadedAt; }
    public long getSizeBytes() { return sizeBytes; }

    public String getFormattedSize() {
        if (sizeBytes < 1024) return sizeBytes + " B";
        if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
        return String.format("%.1f MB", sizeBytes / (1024.0 * 1024));
    }
}
