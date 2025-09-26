package com.dobbinsoft.gus.remotesales.client.gus.file;

import com.dobbinsoft.gus.remotesales.client.gus.file.model.AttachmentVO;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "file-attachment", url = "http://gus-file", path = "/api/attachment")
public interface FileAttachmentFeignClient {

    @Operation(summary = "临时上传附件")
    @PostMapping("/upload-temp")
    R<AttachmentVO> uploadTemp(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "previewExpirySeconds", required = false) Integer previewExpirySeconds);

    @Operation(summary = "确认附件为永久")
    @PostMapping("/{id}/confirm")
    R<AttachmentVO> confirm(@PathVariable("id") String id);

    @Operation(summary = "获取签名URL")
    @GetMapping("/{id}/signed-url")
    R<String> signedUrl(@PathVariable("id") String id,
                               @RequestParam(value = "expirySeconds", required = false) Integer expirySeconds);

    @Operation(summary = "删除附件")
    @DeleteMapping("/{id}")
    R<Void> delete(@PathVariable("id") String id);

}
