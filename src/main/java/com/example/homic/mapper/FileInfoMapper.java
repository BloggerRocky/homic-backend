package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.file.FileInfo;
import com.example.homic.model.file.FileInfoKey;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileInfoMapper  extends BaseMapper<FileInfo> {
    int deleteByPrimaryKey(FileInfoKey key);

    int insert(FileInfo record);

    int insertSelective(FileInfo record);

    FileInfo selectByPrimaryKey(FileInfoKey key);

    int updateByPrimaryKeySelective(FileInfo record);
    int updateByPrimaryKey(FileInfo record);
    FileInfo selectOneByFileMd5 (String fileMd5);
    //获取用户已用空间大小
    Long getSizeByUserId(String userId);
    //获取家庭已用空间大小
    Long getSizeByFamilyId(String familyId);
    //获取文件的子文件
    String[] getSubFileById(String[] fileIds);
    //根据文件ID和用户ID查询文件
    FileInfo selectByFileIdAndUserId(String fileId, String userId);
}
