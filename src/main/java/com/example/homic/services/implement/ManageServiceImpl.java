package com.example.homic.services.implement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.FileInfoMapper;
import com.example.homic.model.file.FileInfo;
import com.example.homic.services.ManageService;
import com.example.homic.utils.MinioUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.FileInfoVO;
import com.example.homic.vo.FolderInfoVO;
import com.example.homic.vo.PageResultVO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.homic.constants.CodeConstants.FAIL_RES_CODE;
import static com.example.homic.constants.NormalConstants.*;
import static com.example.homic.constants.NormalConstants.FOLDER_TYPE_FOLDER;
import static com.example.homic.constants.enums.FileFlagEnum.NORMAL;
import static com.example.homic.constants.enums.FileFlagEnum.RECYCLE;
import static com.example.homic.constants.enums.FileStatusEnum.TRANS_SUCCEED;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/18.13:59
 * 项目名：homic
 */
@Service
public class ManageServiceImpl extends CommonServiceImpl implements ManageService {
    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    MinioUtils minioUtils;
    private static Logger logger = LoggerFactory.getLogger(ManageServiceImpl.class);

    /**
     * 创建文件夹
     *
     * @param filePid
     * @param fileName
     * @param userId
     * @return
     */
    @Override
    public FileInfoVO newFolder(String filePid, String fileName, String userId) throws MyException {
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getUserId, userId);
        fileInfoLqw.eq(FileInfo::getFilePid, filePid);
        fileInfoLqw.eq(FileInfo::getFileName, fileName);
        fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
        fileInfoLqw.eq(FileInfo::getFolderType, FOLDER_TYPE_FOLDER);
        try {
            if (fileInfoMapper.selectCount(fileInfoLqw) > 0) {
                return null;
            } else {
                FileInfo fileInfo = new FileInfo();
                String fileId = StringUtils.getSerialNumber(15);
                fileInfo.setFileId(fileId);
                fileInfo.setUserId(userId);
                fileInfo.setFilePid(filePid);
                fileInfo.setFileName(fileName);
                Date date = new Date();
                fileInfo.setCreateTime(date);
                fileInfo.setLastUpdateTime(date);
                fileInfo.setFileCover(FILE_SYSTEM_PATH + DEFAULT_FOLDER_ICON_NAME);
                fileInfo.setFolderType(FOLDER_TYPE_FOLDER);
                fileInfo.setDelFlag(NORMAL.getFlag());
                fileInfo.setStatus(TRANS_SUCCEED.getStatus());
                fileInfoMapper.insertSelective(fileInfo);
                return modelMapper.map(fileInfo, FileInfoVO.class);
            }
        } catch (Exception e) {
            logger.error("创建文件夹时发生异常", e);
            throw new MyException("创建文件夹时发生异常", FAIL_RES_CODE);
        }
    }

    /**
     * 文件/文件夹重命名
     *
     * @param fileId
     * @param fileName
     * @param userId
     * @return
     */
    @Override
    public FileInfoVO renameFile(String fileId, String fileName, String userId) {
        //查询该文件的父文件夹Id（即filePid）和目录类型（folderType)
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getFileId, fileId);
        fileInfoLqw.eq(FileInfo::getUserId, userId);
        try {
            FileInfo fileInfo = fileInfoMapper.selectOne(fileInfoLqw);
            Integer folderType = fileInfo.getFolderType();
            String filePid = fileInfo.getFilePid();
            //查询该父文件夹下是否有相同姓名的同目录类型文件
            LambdaQueryWrapper<FileInfo> fileInfoLqw2 = new LambdaQueryWrapper<>();
            fileInfoLqw2.eq(FileInfo::getFileName, fileName);
            fileInfoLqw2.eq(FileInfo::getFilePid, filePid);
            if (fileInfoMapper.selectCount(fileInfoLqw2) > 0)
                return null;
            //满足要求更新数据库
            fileInfo.setFileName(fileName);
            fileInfo.setLastUpdateTime(new Date());
            fileInfoMapper.updateByPrimaryKeySelective(fileInfo);//自动装填
            //回传文件信息
            return modelMapper.map(fileInfo, FileInfoVO.class);
        } catch (Exception e) {
            logger.error("重命名文件时发生异常", e);
            return null;
        }
    }

    /**
     * @param path
     * @param shareId
     * @param userId
     * @return
     * @TODO ：获取当前文件夹路径中各文件夹的信息
     */
    @Override
    public List<FolderInfoVO> getFolderInfo(String path, String shareId, String userId) {
        String[] folderIds = path.split("/");
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.in(FileInfo::getFileId, folderIds);
        fileInfoLqw.eq(FileInfo::getUserId, userId);
        fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
        fileInfoLqw.eq(FileInfo::getFolderType, FOLDER_TYPE_FOLDER);
        List<FileInfo> folderInfos = fileInfoMapper.selectList(fileInfoLqw);
        List<FolderInfoVO> folderInfoVOs = folderInfos.stream().map(folderInfo -> modelMapper.map(folderInfo, FolderInfoVO.class)).collect(Collectors.toList());
        return folderInfoVOs;
    }

    /**
     * 获取filePid文件下所有文件但不包含当前fileId
     *
     * @param filePid
     * @param currentFileIds
     * @param userId
     * @return
     */
    @Override
    public List<FileInfoVO> loadAllFolder(String filePid, String currentFileIds, String userId) throws MyException {
        //查询当前目录中fleId不在当前多个文件id中的目录
        String[] idArray = currentFileIds.split(",");
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getFilePid, filePid);
        fileInfoLqw.eq(FileInfo::getUserId, userId);
        fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
        fileInfoLqw.eq(FileInfo::getFolderType, FOLDER_TYPE_FOLDER);
        fileInfoLqw.notIn(FileInfo::getFileId, idArray);
        try {
            List<FileInfo> fileInfos = fileInfoMapper.selectList(fileInfoLqw);
            List<FileInfoVO> fileInfoVOs = fileInfos.stream().map(fileInfo -> modelMapper.map(fileInfo, FileInfoVO.class)).collect(Collectors.toList());
            return fileInfoVOs;
        } catch (Exception e) {
            throw new MyException("获取文件夹信息失败", FAIL_RES_CODE);
        }
    }

    /**
     * 移动/批量移动 文件
     *
     * @param fileIds
     * @param filePid
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public List<FileInfoVO> changeFileFolder(String fileIds, String filePid, String userId) throws MyException {
        try {
            String[] fileIdArray = fileIds.split(",");
            LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
            fileInfoLqw.in(FileInfo::getFileId, fileIdArray);
            fileInfoLqw.eq(FileInfo::getUserId, userId);
            fileInfoLqw.eq(FileInfo::getFilePid, filePid);
            if (fileInfoMapper.selectCount(fileInfoLqw) > 0)
                throw new MyException("目标文件夹下已存在同名文件", FAIL_RES_CODE);
            LambdaQueryWrapper<FileInfo> fileInfoLqw2 = new LambdaQueryWrapper<>();
            fileInfoLqw2.in(FileInfo::getFileId, fileIdArray);
            fileInfoLqw2.eq(FileInfo::getUserId, userId);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePid(filePid);
            fileInfo.setLastUpdateTime(new Date());
            fileInfoMapper.update(fileInfo, fileInfoLqw2);
            return null;
        } catch (MyException e) {
            throw new MyException("移动文件失败", FAIL_RES_CODE);
        }
    }

    /**
     * 预删除文件：将文件放入回收站
     *
     * @param fileIds
     * @param userId
     * @return
     */
    @Override
    public boolean deleteFile(String fileIds, String userId) throws MyException {
        //将所有待删除文件的状态改成回收站
        List<String> fileIdsList = List.of(fileIds.split(","));
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.in(FileInfo::getFileId, fileIdsList);
        fileInfoLqw.eq(FileInfo::getUserId, userId);
        fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
        FileInfo fileInfo = new FileInfo();
        //设置回收时间和删除状态
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setDelFlag(RECYCLE.getFlag());
        try {
            fileInfoMapper.update(fileInfo, fileInfoLqw);
        } catch (Exception e) {
            logger.error("数据库异常", e);
            throw new MyException("删除文件失败", FAIL_RES_CODE);
        }
        return true;
    }

    /**
     * 加载回收站目录列表
     *
     * @param pageNoStr
     * @param pageSizeStr
     * @param userId
     * @return
     */
    @Override
    public PageResultVO loadRecycleList(String pageNoStr, String pageSizeStr, String userId) {
        Long pageNo = pageNoStr.equals("") ? 1L : Long.parseLong(pageNoStr);
        Long pageSize = pageSizeStr.equals("") ? DEFAULT_PAGE_SIZE : Long.parseLong(pageSizeStr);
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getUserId, userId);
        fileInfoLqw.eq(FileInfo::getDelFlag, RECYCLE.getFlag());
        IPage<FileInfo> page = new Page<>(pageNo, pageSize);
        fileInfoMapper.selectPage(page, fileInfoLqw);
        PageResultVO pageResultVO = new PageResultVO(page, FileInfo.class);
        return pageResultVO;
    }

    /**
     * 批量恢复文件或者文件夹
     *
     * @param ids_str //文件id以，为间隔构成的字符串
     * @param userId
     * @return
     */
    @Override
    public boolean recoverFile(String ids_str, String userId) throws MyException {
        String[] fileIds = ids_str.split(",");
        for (String fileId : fileIds) {
            try {
                LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
                fileInfoLqw.eq(FileInfo::getFileId, fileId);
                fileInfoLqw.eq(FileInfo::getUserId, userId);
                FileInfo fileInfo = fileInfoMapper.selectOne(fileInfoLqw);
                System.out.println(isPathValid(fileInfo.getFilePid(), userId));
                if (isPathValid(fileInfo.getFilePid(), userId)) {
                    //如果路径有效，直接恢复为正常
                    LambdaQueryWrapper<FileInfo> fileInfoLqw2 = new LambdaQueryWrapper<>();
                    fileInfoLqw2.eq(FileInfo::getFileId, fileId);
                    fileInfoLqw2.eq(FileInfo::getUserId, userId);
                    FileInfo updateInfo = new FileInfo();
                    updateInfo.setDelFlag(NORMAL.getFlag());
                    System.out.println(fileInfoMapper.selectOne(fileInfoLqw2).getFileId());
                    fileInfoMapper.update(updateInfo, fileInfoLqw2);
                    System.out.println("55555");
                } else {
                    //如果路径无效，恢复为正常放到根目录
                    LambdaQueryWrapper<FileInfo> fileInfoLqw3 = new LambdaQueryWrapper<>();
                    fileInfoLqw3.eq(FileInfo::getFileId, fileId);
                    fileInfoLqw3.eq(FileInfo::getUserId, userId);
                    FileInfo updateInfo = new FileInfo();
                    updateInfo.setDelFlag(NORMAL.getFlag());
                    updateInfo.setFilePid("0");
                    fileInfoMapper.update(updateInfo, fileInfoLqw3);
                }
            } catch (Exception e) {
                throw new MyException("恢复文件失败", FAIL_RES_CODE);
            }
        }
        return true;
    }


    //查询文件夹的路径是否完全有效（即文件路径上的文件夹全部存在）
    public boolean isPathValid(String filePid, String userId) {
        if (filePid.equals("0"))
            return true;
        else {
            LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
            fileInfoLqw.eq(FileInfo::getFileId, filePid);
            fileInfoLqw.eq(FileInfo::getUserId, userId);
            fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
            fileInfoLqw.eq(FileInfo::getFolderType, FOLDER_TYPE_FOLDER);
            FileInfo fileInfo = fileInfoMapper.selectOne(fileInfoLqw);
            if (fileInfo == null)
                return false;
            else
                return isPathValid(fileInfo.getFilePid(), userId);
        }
    }

    /**
     * 彻底 删除文件
     * @param fileIds
     * @param userId
     * @return
     */
    @Override
    public boolean smashFile(List<String> fileIds, String userId) throws Exception {
        try{
            deleteTotalFile(fileIds,userId);
            refreshUseSpace(userId);
        }catch (Exception e)
        {
            logger.error("数据库异常", e);
            return false;
        }
        return true;
    }
    //删除文件和它们的子文件
    @Transactional
    void deleteTotalFile(List<String> fileIdsList, String userId) throws Exception {
        //获取当前文件信息
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.in(FileInfo::getFileId, fileIdsList);
        fileInfoLqw.eq(FileInfo::getUserId, userId);
        List<FileInfo> fileInfos = fileInfoMapper.selectList(fileInfoLqw);
        //查询出当前文件信息后就可以直接删除数据库记录
        fileInfoMapper.delete(fileInfoLqw);
        List<String> subFileIds = new ArrayList<>();
        for (FileInfo info : fileInfos) {

            // 【1】 对于文件，如果没有其他文件引用相同的实体文件，删除对应的实体文件
            if(info.getFolderType() == FOLDER_TYPE_FILE){
                String md5 = info.getFileMd5();
                LambdaQueryWrapper<FileInfo> fileInfoLqw2 = new LambdaQueryWrapper<>();
                fileInfoLqw2.eq(FileInfo::getFileMd5, md5);
                //查询相同md5值的文件数量为0，删除实体文件
                if (fileInfoMapper.selectCount(fileInfoLqw2) == 0) {
                    String folderPath = info.getFilePath().substring(0,info.getFilePath().lastIndexOf("/"));
                    minioUtils.deleteFolder(folderPath);
                }
            }
            // 【2】 对于文件夹，将子文件加入待删除列表
            if (info.getFolderType() == FOLDER_TYPE_FOLDER) {   //查询该文件夹的子文件
                LambdaQueryWrapper<FileInfo> fileInfoLqw3 = new LambdaQueryWrapper<>();
                fileInfoLqw3.eq(FileInfo::getFilePid, info.getFileId());
                fileInfoLqw3.eq(FileInfo::getUserId, userId);
                List<FileInfo> subFileInfos = fileInfoMapper.selectList(fileInfoLqw3);
                //将子文件id加入待删除列表
                for (FileInfo subInfo : subFileInfos) {
                    subFileIds.add(subInfo.getFileId());
                }
            }
            //如果这些文件还有子文件，递归执行删除
            if (subFileIds.size() > 0)
                deleteTotalFile(subFileIds, userId);
        }

    }
}
