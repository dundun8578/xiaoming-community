package com.xiaoming.module.minio.service;
import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;
@Service
public class MinioService {
    private final MinioClient client;
    private final String bucket;
    private volatile boolean initialized;
    public MinioService(MinioClient client,@Value("${minio.bucket-name}") String bucket) {
        this.client=client;this.bucket=bucket;
    }
    private synchronized void ensureBucket() throws Exception {
        if(initialized)return;
        if(!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())){
            try {client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());}
            catch(io.minio.errors.ErrorResponseException e){
                if(!"BucketAlreadyOwnedByYou".equals(e.errorResponse().code()))throw e;
            }
        }
        initialized=true;
    }
    public String uploadFile(MultipartFile file,String folder) {
        if(file==null || file.isEmpty())throw new IllegalArgumentException("文件不能为空");
        if(file.getSize()>100L*1024*1024)throw new IllegalArgumentException("文件不能超过100MB");
        String contentType=file.getContentType();
        boolean image=folder.startsWith("avatar") || folder.startsWith("note");
        java.util.Map<String,String> extensions=java.util.Map.of("image/jpeg",".jpg","image/png",".png",
            "image/gif",".gif","image/webp",".webp","video/mp4",".mp4","video/webm",".webm");
        if(contentType==null || !extensions.containsKey(contentType) ||
                (image && !contentType.startsWith("image/")) || (!image && !contentType.startsWith("video/")))
            throw new IllegalArgumentException("不支持此文件类型");
        String object=folder+"/"+UUID.randomUUID()+extensions.get(contentType);
        try {
            ensureBucket();
            try(InputStream input=file.getInputStream()){
                client.putObject(PutObjectArgs.builder().bucket(bucket).object(object)
                    .stream(input,file.getSize(),-1).contentType(contentType).build());
            }
            return getFileUrl(object);
        }catch(Exception e){throw new IllegalStateException("文件上传失败",e);}
    }
    public String uploadAvatar(MultipartFile file){return uploadFile(file,"avatar");}
    public String uploadNoteImage(MultipartFile file){return uploadFile(file,"note/image");}
    public String uploadNoteCover(MultipartFile file){return uploadFile(file,"note/cover");}
    public String uploadVideo(MultipartFile file){return uploadFile(file,"video");}
    public void deleteFile(String object){
        throw new IllegalArgumentException("文件由笔记引用，暂不支持单独删除");
    }
    public String getFileUrl(String object){return "/api/file/content/"+object;}
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> readFile(String object) {
        if(object.contains("..") || !object.matches("(avatar|note/image|note/cover|video)/[a-f0-9-]+\\.(jpg|png|gif|webp|mp4|webm)"))
            throw new IllegalArgumentException("文件路径无效");
        try{
            StatObjectResponse stat=client.statObject(StatObjectArgs.builder().bucket(bucket).object(object).build());
            InputStream input=client.getObject(GetObjectArgs.builder().bucket(bucket).object(object).build());
            return org.springframework.http.ResponseEntity.ok()
                .header("X-Content-Type-Options","nosniff").contentLength(stat.size())
                .contentType(org.springframework.http.MediaType.parseMediaType(stat.contentType()))
                .body(new org.springframework.core.io.InputStreamResource(input));
        }catch(Exception e){throw new IllegalArgumentException("文件不存在或存储服务暂不可用");}
    }
}
