package com.example.homic.utils;

import com.example.homic.config.MinioConfig;
import com.example.homic.config.properties.MinioProperties;
import com.example.homic.exception.MyException;
import io.minio.*;

import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.homic.constants.CodeConstants.FAIL_RES_CODE;
import static com.example.homic.constants.enums.DatePatternEnum.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/15.22:19
 * 项目名：homic
 */
//minio工具类
@Component("minioUtils")
public class MinioUtils {
    @Autowired
     MinioProperties minioProperties;
    @Autowired
     MinioClient minioClient;
    //获取文件返回体
    static Logger logger = LoggerFactory.getLogger(MinioUtils.class);
    /**
     * 初始化bucket，如果不存在则创建，并设置为公开访问策略
     */
    @PostConstruct
    public void initBucket() {
        try {
            String bucketName = minioProperties.getBucketName();

            // 检查bucket是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            // 如果不存在则创建
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                logger.info("创建bucket成功: {}", bucketName);
            }

            // 设置bucket为公开访问策略（允许读写）
            String policy = "{\n" +
                    "  \"Version\": \"2012-10-17\",\n" +
                    "  \"Statement\": [\n" +
                    "    {\n" +
                    "      \"Effect\": \"Allow\",\n" +
                    "      \"Principal\": {\"AWS\": \"*\"},\n" +
                    "      \"Action\": [\n" +
                    "        \"s3:GetObject\",\n" +
                    "        \"s3:PutObject\",\n" +
                    "        \"s3:DeleteObject\"\n" +
                    "      ],\n" +
                    "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"Effect\": \"Allow\",\n" +
                    "      \"Principal\": {\"AWS\": \"*\"},\n" +
                    "      \"Action\": \"s3:ListBucket\",\n" +
                    "      \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );

            logger.info("设置bucket访问策略成功: {}", bucketName);

        } catch (Exception e) {
            logger.error("初始化MinIO bucket失败", e);
        }
    }

    public  GetObjectResponse getFileResponse(String filePath) throws Exception{
        GetObjectResponse objectResponse = null;
        try {
            objectResponse = minioClient.getObject(GetObjectArgs.
                    builder()
                    .bucket(minioProperties.getBucketName())
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            return null;
        }
        return objectResponse;
    }
    //将文件写入response
    public  void getFile(String filePath, HttpServletResponse response) throws Exception{
        //指定下载文件的名称和后缀
        String suffix = filePath.substring(filePath.lastIndexOf("."));
        Date date = new Date();
        String time = StringUtils.formatDate(date,YEAR_MONTH_DAY_HOUR_MINUTE_SECOND.getPattern());
        String fileName = time + suffix;
        try {
            String type = filePath.substring(filePath.lastIndexOf("."));
            GetObjectResponse objectResponse = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .object(filePath)
                    .build());
            // 设置Content-Disposition头，告诉浏览器这是一个附件，并指定文件名
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            // 设置Content-Type，设置”octet-stream“可以使得下载请求发送后有确认阶段，而非直接下载
            response.setContentType("application/octet-stream");
            // 设置文件大小信息，在获取下载请求后客户端就可以得知文件大小
            String length = objectResponse.headers().get("Content-Length");//从minIO请求返回体的header中读取
            response.setContentLength(Integer.parseInt(length));
            // 输出文件
            objectResponse.transferTo(response.getOutputStream());
        } catch (Exception e) {
        }
    }
    //删除文件
    public  void deleteFile(String filePath) throws Exception{

        try {
            minioClient.removeObject(
                    RemoveObjectArgs
                            .builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filePath)
                            .build());
        } catch (Exception e) {
            throw new MyException("删除原文件失败",FAIL_RES_CODE);
        }
    }
    //保存MultipartFile文件
    public boolean saveMultipartFile(String filePath, MultipartFile file) throws Exception{
        InputStream inputStream = file.getInputStream();
        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .stream(inputStream,file.getSize(),-1)//传入输入流
                    .bucket(minioProperties.getBucketName())
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            throw new MyException("上传新文件失败",FAIL_RES_CODE);
        }finally {
            inputStream.close();
        }
        return true;
    }
    //保存File文件
    public boolean saveFile(String filePath, File file) throws Exception{
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .stream(fileInputStream,fileInputStream.available(),-1)//inputStream.available()可以返回输入流的可读字节数传入输入流
                    .bucket(minioProperties.getBucketName())
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            throw new MyException("上传新文件失败",FAIL_RES_CODE);
        }finally {
            fileInputStream.close();
        }
        return true;
    }
    //删除文件夹（实际是删除文件夹下的所有文件）
    public boolean deleteFolder(String folderPath) throws Exception{
        try {
            //获取该文件夹下的所有文件签名
            Iterable<Result<Item>> items = minioClient.listObjects(ListObjectsArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .prefix(folderPath+"/")//注意要在文件夹名称后面加”/“
                    .recursive(true)
                    .build());
            //根据文件签名挨个删除文件
            for(Result<Item> itemResult : items)
            {
                String filePath = itemResult.get().objectName();
                minioClient.removeObject(
                        RemoveObjectArgs
                                .builder()
                                .bucket(minioProperties.getBucketName())
                                .object(filePath)
                                .build());
            }
        }catch (Exception e)
        {
            throw e;
        }
        return true;
    }


    //检查文件是否存在
    public boolean checkExist(String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs
                            .builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filePath)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
