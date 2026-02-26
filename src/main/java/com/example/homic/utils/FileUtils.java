package com.example.homic.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.0:27
 * 项目名：homic
 */
//用于处理服务器本地文件的工具类
public class FileUtils {
    public static Logger logger = LoggerFactory.getLogger(FileUtils.class);
    public static void downloadFile(String filePath, HttpServletResponse response) throws IOException {
        //获取文件后缀，指定输出类型
        String type = filePath.substring(filePath.lastIndexOf(".")+1);
        response.setContentType("image//"+type);
        //创建输入流和输出流
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        OutputStream outputStream = response.getOutputStream();
        write(fileInputStream, outputStream);
        fileInputStream.close();
        outputStream.flush();
        outputStream.close();
    }

    private static void write(InputStream inputStream, OutputStream outputStream) throws IOException {
        //创建缓冲区
        byte[] buffer = new byte[1024];
        int len = 0;
        //fileInputStream.read(buffer)将文件数据写入buffer缓冲区中,返回值为写入长度
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
    }

    public  static  boolean deleteFile(String filePath)  {
        File file = new File(filePath);
        try{
            if (file.exists())
                file.delete();
        }catch (Exception e)
        {
            logger.error("删除文件失败");
            return false;
        }
        return true;
    }
    public  static void deleteFolder(String folderPath)
    {
        try {
            File folder = new File(folderPath);
            if(folder.exists() && folder.isDirectory())
            {
                File[] files = folder.listFiles();
                for(File childFile :files)
                {
                    if(childFile.isDirectory())
                        deleteFolder(childFile.getAbsolutePath());
                    childFile.delete();
                }
            }
            folder.delete();
        } catch (Exception e) {
            logger.error("删除文件夹失败");
            throw e;
        }
    }
    public  static  boolean saveFile(String filePath, MultipartFile file) throws IOException {
        File dest = new File(filePath);
        try{
            //如果父文件夹不存在则创建
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
        }catch (Exception e)
        {
            logger.error("保存文件失败");
            return false;
        }
        return true;
    }
}
