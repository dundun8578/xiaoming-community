package com.xiaoming.module.minio.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO对象存储服务
 */
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private String bucketName;

    /**
     * 上传文件（通用）
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = folder + "/" + UUID.randomUUID() + extension;

            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            inputStream.close();
            return bucketName + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, "avatar");
    }

    /**
     * 上传笔记图片
     */
    public String uploadNoteImage(MultipartFile file) {
        return uploadFile(file, "note/image");
    }

    /**
     * 上传笔记封面
     */
    public String uploadNoteCover(MultipartFile file) {
        return uploadFile(file, "note/cover");
    }

    /**
     * 上传视频
     */
    public String uploadVideo(MultipartFile file) {
        return uploadFile(file, "video");
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件URL
     */
    public String getFileUrl(String objectName) {
        return "/" + bucketName + "/" + objectName;
    }
}
