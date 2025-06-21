package com.sai.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file) throws FileNotFoundException {
        File fileObject = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName,fileName,fileObject));

        // after fileUpload to s3 bucket delete file; otherwise it will keep on add from app directory
        fileObject.delete();
        return "File Uploaded "+fileName;
    }

    public byte[] downloadFile(String fileName) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName,fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();

        return IOUtils.toByteArray(inputStream);
    }

    private File convertMultiPartFileToFile(MultipartFile file) throws FileNotFoundException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try(FileOutputStream fileOutputStream = new FileOutputStream(convertedFile)){
                fileOutputStream.write(file.getBytes());
        }catch (IOException e){
            log.error("error converting multipart file to file", e);
        }
        return convertedFile;
    }

    public String deleteFile(String fileName){
      s3Client.deleteObject(bucketName,fileName);
        return "deleted file_"+fileName;
    }

}
