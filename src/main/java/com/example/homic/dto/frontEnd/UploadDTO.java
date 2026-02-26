package com.example.homic.dto.frontEnd;

import org.springframework.web.multipart.MultipartFile;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.22:00
 * 项目名：homic
 */
//用于存放前端上传文件信息的包装实体类
public class UploadDTO {
    private String fileId;
    private MultipartFile file;
    private String fileName;
    private String filePid;
    private String fileMd5;
    private Integer chunkIndex;//分片索引
    private Integer chunks;//总分片数

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePid() {
        return filePid;
    }

    public void setFilePid(String filePid) {
        this.filePid = filePid;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getChunks() {
        return chunks;
    }

    public void setChunks(Integer chunks) {
        this.chunks = chunks;
    }

    public UploadDTO() {
    }

    @Override
    public String toString() {
        return "UploadDTO{" +
                "fileId='" + fileId + '\'' +
                ", file=" + file +
                ", fileName='" + fileName + '\'' +
                ", filePid='" + filePid + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", chunkIndex='" + chunkIndex + '\'' +
                ", chucks='" + chunks + '\'' +
                '}';
    }
}
