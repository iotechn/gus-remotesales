package com.dobbinsoft.gus.remotesales.controller.wecom;

import com.dobbinsoft.gus.remotesales.client.gus.file.model.AttachmentVO;
import com.dobbinsoft.gus.remotesales.service.OssService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "企业微信OSS接口")
@Slf4j
@RequestMapping("/wecom/oss")
public class WecomOssController {

    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public R<AttachmentVO> upload(@RequestParam("file") MultipartFile file) {
        return R.success(ossService.upload(file));
    }

}
