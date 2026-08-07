package com.xiaoming.module.minio.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.minio.service.MinioService;
import io.swagger.v3.annotations.Api;
import io.swagger.v3.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
{tags = "文件上传模块")
public class FileUploadController {

    @Autowired
    private MinioService minioService;

    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = minioService.uploadAvatar(file);
        return Result.ok(url);
    }

    @PostMapping("/upload/image")
    public Result<String> uploadNoteImage(@RequestParam("file") MultipartFile file) {
        String url = minioService.uploadNoteImage(file);
        return Result.ok(url);
    }

    @PostMapping("/upload/cover")
    public Result<String> uploadNoteCover(@RequestParam("file") MultipartFile file) {
        String url = minioService.uploadNoteCover(file);
        return Result.ok(url);
    }

    @PostMapping("/upload/video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        String url = minioService.uploadVideo(file);
        return Result.ok(url);
    }

    @DeleteMapping("/{objectName}")
    public Result<Void> deleteFile(@PathVariable String objectName) {
        minioService.deleteFile(objectName);
        return Result.ok();
    }
}
