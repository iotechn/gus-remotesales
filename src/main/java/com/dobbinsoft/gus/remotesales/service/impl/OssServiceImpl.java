package com.dobbinsoft.gus.remotesales.service.impl;

import com.dobbinsoft.gus.remotesales.client.gus.file.FileAttachmentFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.file.model.AttachmentVO;
import com.dobbinsoft.gus.remotesales.service.OssService;
import com.dobbinsoft.gus.web.exception.BasicErrorCode;
import com.dobbinsoft.gus.web.exception.ServiceException;
import com.dobbinsoft.gus.web.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class OssServiceImpl implements OssService {

    @Autowired
    private FileAttachmentFeignClient fileAttachmentFeignClient;

    @Override
    public AttachmentVO upload(MultipartFile file){
        R<AttachmentVO> attachmentVOR = fileAttachmentFeignClient.uploadTemp(file, null);
        if (!BasicErrorCode.SUCCESS.getCode().equals(attachmentVOR.getCode())){
            throw new ServiceException(attachmentVOR.getCode(), attachmentVOR.getMessage());
        }
        return attachmentVOR.getData();
    }

}
