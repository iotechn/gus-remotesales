package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.client.gus.file.model.AttachmentVO;
import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    AttachmentVO upload(MultipartFile file);

}
