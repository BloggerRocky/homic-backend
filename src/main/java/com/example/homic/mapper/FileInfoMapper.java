package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.FileInfo;
import com.example.homic.model.FileInfoKey;
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
    //获取文件的子文件
    String[] getSubFileById(String[] fileIds);
}
