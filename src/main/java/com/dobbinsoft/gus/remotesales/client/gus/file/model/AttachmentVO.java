package com.dobbinsoft.gus.remotesales.client.gus.file.model;

import lombok.Data;

@Data
public class AttachmentVO {
    private String id;
    private String name;
    private String storagePath;
    private Long fileSize;
    private String mimeType;
    private String fileExtension;
    private String md5Hash;
    private String status; // TEMP or PERMANENT
    private String previewUrl; // for TEMP
}


