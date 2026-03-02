package com.example.homic.services.implement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.homic.config.RedisManager;
import com.example.homic.constants.enums.FileFlagEnum;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.FileInfoMapper;
import com.example.homic.mapper.FriendFileShareMapper;
import com.example.homic.mapper.FriendRelationMapper;
import com.example.homic.model.FriendFileShare;
import com.example.homic.model.FriendRelation;
import com.example.homic.model.file.FileInfo;
import com.example.homic.services.FriendFileShareService;
import com.example.homic.utils.MinioUtils;
import com.example.homic.utils.RedisUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.FriendShareVO;
import com.example.homic.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

import static com.example.homic.constants.CodeConstants.FAIL_RES_CODE;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_CODE;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;
import static com.example.homic.constants.NormalConstants.FILE_DEFAULT_M3U8_NAME;
import static com.example.homic.constants.NormalConstants.SESSION_VIDEO_PATH_KEY;
import static com.example.homic.constants.enums.FileStatusEnum.TRANS_SUCCEED;
import static com.example.homic.constants.enums.FileTypeEnum.VIDEO;

/**
 * 好友文件分享服务实现类
 * 作者：Rocky23318
 * 时间：2026
 * 项目名：homic
 */
@Service
public class FriendFileShareServiceImpl implements FriendFileShareService {

    private static final Logger logger = LoggerFactory.getLogger(FriendFileShareServiceImpl.class);

    @Autowired
    private FriendFileShareMapper friendFileShareMapper;

    @Autowired
    private FriendRelationMapper friendRelationMapper;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private RedisManager redisManager;

    @Autowired
    private MinioUtils minioUtils;

    /**
     * 分享文件给好友
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO shareFilesToFriend(String userId, String friendId, String fileIds, Integer validType) throws MyException {
        try {
            // 验证是否是好友关系
            FriendRelation relation = friendRelationMapper.selectRelation(userId, friendId);
            if (relation == null || relation.getStatus() != 1) {
                throw new MyException("只能向好友分享文件", FAIL_RES_CODE);
            }

            String[] fileIdArray = fileIds.split(",");
            int successCount = 0;

            for (String fileId : fileIdArray) {
                // 查询文件信息
                FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
                if (fileInfo == null) {
                    logger.warn("文件不存在或无权限: {}", fileId);
                    continue;
                }

                // 创建分享记录
                FriendFileShare share = new FriendFileShare();
                share.setFromUserId(userId);
                share.setToUserId(friendId);
                share.setFileId(fileId);
                share.setFileName(fileInfo.getFileName());
                share.setFolderType(fileInfo.getFolderType());
                share.setFileType(fileInfo.getFileType());
                share.setFileCategory(fileInfo.getFileCategory());
                share.setFileCover(fileInfo.getFileCover());
                share.setFileSize(fileInfo.getFileSize());
                share.setValidType(validType);
                share.setShareTime(new Date());
                share.setStatus(1);

                // 计算过期时间
                if (validType != null && validType > 0) {
                    long expireMillis = System.currentTimeMillis();
                    switch (validType) {
                        case 1: expireMillis += 24 * 60 * 60 * 1000L; break;  // 1天
                        case 2: expireMillis += 7 * 24 * 60 * 60 * 1000L; break;  // 7天
                        case 3: expireMillis += 30 * 24 * 60 * 60 * 1000L; break;  // 30天
                    }
                    share.setExpireTime(new Date(expireMillis));
                }

                friendFileShareMapper.insert(share);
                successCount++;
            }

            logger.info("用户 {} 向好友 {} 分享了 {} 个文件", userId, friendId, successCount);

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("成功分享 " + successCount + " 个文件");
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("分享文件失败", e);
            throw new MyException("分享文件失败", FAIL_RES_CODE);
        }
    }

    /**
     * 查询与好友的分享历史记录
     */
    @Override
    public ResponseVO getShareHistory(String userId, String friendId) throws MyException {
        try {
            List<FriendShareVO> shareList = friendFileShareMapper.selectShareHistory(userId, friendId);

            // 更新过期状态
            for (FriendShareVO share : shareList) {
                if (share.getStatus() == 1 && share.getExpireTime() != null) {
                    if (share.getExpireTime().before(new Date())) {
                        FriendFileShare updateShare = new FriendFileShare();
                        updateShare.setShareId(share.getShareId());
                        updateShare.setStatus(2);  // 已过期
                        friendFileShareMapper.updateById(updateShare);
                        share.setStatus(2);
                    }
                }
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setData(shareList);
            return responseVO;
        } catch (Exception e) {
            logger.error("查询分享历史失败", e);
            throw new MyException("查询分享历史失败", FAIL_RES_CODE);
        }
    }

    /**
     * 取消分享
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO cancelFriendShare(String userId, String shareIds) throws MyException {
        try {
            String[] shareIdArray = shareIds.split(",");
            int successCount = 0;

            for (String shareIdStr : shareIdArray) {
                Long shareId = Long.parseLong(shareIdStr);
                FriendFileShare share = friendFileShareMapper.selectById(shareId);

                if (share != null && share.getFromUserId().equals(userId)) {
                    share.setStatus(0);  // 已取消
                    friendFileShareMapper.updateById(share);
                    successCount++;
                }
            }

            logger.info("用户 {} 取消了 {} 个分享", userId, successCount);

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("成功取消 " + successCount + " 个分享");
            return responseVO;
        } catch (Exception e) {
            logger.error("取消分享失败", e);
            throw new MyException("取消分享失败", FAIL_RES_CODE);
        }
    }

    /**
     * 保存好友分享的文件到我的网盘
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO saveFriendSharedFile(String userId, Long shareId, String targetFolderId) throws MyException {
        try {
            // 查询分享记录
            FriendFileShare share = friendFileShareMapper.selectById(shareId);
            if (share == null) {
                throw new MyException("分享不存在", FAIL_RES_CODE);
            }

            // 验证是接收者
            if (!share.getToUserId().equals(userId)) {
                throw new MyException("无权限保存此文件", FAIL_RES_CODE);
            }

            // 验证分享状态
            if (share.getStatus() != 1) {
                throw new MyException("分享已失效", FAIL_RES_CODE);
            }

            // 验证是否过期
            if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
                share.setStatus(2);
                friendFileShareMapper.updateById(share);
                throw new MyException("分享已过期", FAIL_RES_CODE);
            }

            // 查询原文件
            FileInfo sourceFile = fileInfoMapper.selectByFileIdAndUserId(share.getFileId(), share.getFromUserId());
            if (sourceFile == null) {
                throw new MyException("原文件不存在", FAIL_RES_CODE);
            }

            // 检查目标文件夹是否存在
            if (targetFolderId == null || targetFolderId.isEmpty()) {
                targetFolderId = "0";  // 默认保存到根目录
            }

            // 检查目标文件夹是否属于当前用户
            if (!targetFolderId.equals("0")) {
                FileInfo targetFolder = fileInfoMapper.selectByFileIdAndUserId(targetFolderId, userId);
                if (targetFolder == null || targetFolder.getFolderType() != 1) {
                    throw new MyException("目标文件夹不存在", FAIL_RES_CODE);
                }
            }

            // 检查目标文件夹中是否已存在同名文件
            LambdaQueryWrapper<FileInfo> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(FileInfo::getUserId, userId)
                       .eq(FileInfo::getFilePid, targetFolderId)
                       .eq(FileInfo::getFileName, sourceFile.getFileName())
                       .eq(FileInfo::getDelFlag, 0);
            FileInfo existingFile = fileInfoMapper.selectOne(checkWrapper);

            String newFileName = sourceFile.getFileName();
            if (existingFile != null) {
                // 如果存在同名文件，自动重命名
                String baseName = sourceFile.getFileName();
                String extension = "";
                int dotIndex = baseName.lastIndexOf(".");
                if (dotIndex > 0 && sourceFile.getFolderType() == 0) {
                    extension = baseName.substring(dotIndex);
                    baseName = baseName.substring(0, dotIndex);
                }

                int counter = 1;
                while (existingFile != null) {
                    newFileName = baseName + "(" + counter + ")" + extension;
                    checkWrapper.clear();
                    checkWrapper.eq(FileInfo::getUserId, userId)
                               .eq(FileInfo::getFilePid, targetFolderId)
                               .eq(FileInfo::getFileName, newFileName)
                               .eq(FileInfo::getDelFlag, 0);
                    existingFile = fileInfoMapper.selectOne(checkWrapper);
                    counter++;
                }
            }

            // 复制文件到目标用户的网盘
            FileInfo newFile = new FileInfo();
            newFile.setFileId(StringUtils.getSerialNumber(15));
            newFile.setUserId(userId);
            newFile.setFilePid(targetFolderId);
            newFile.setFileName(newFileName);
            newFile.setFileMd5(sourceFile.getFileMd5());
            newFile.setFileSize(sourceFile.getFileSize());
            newFile.setFileCover(sourceFile.getFileCover());
            newFile.setFilePath(sourceFile.getFilePath());
            newFile.setFolderType(sourceFile.getFolderType());
            newFile.setFileCategory(sourceFile.getFileCategory());
            newFile.setFileType(sourceFile.getFileType());
            newFile.setStatus(sourceFile.getStatus());
            newFile.setCreateTime(new Date());
            newFile.setLastUpdateTime(new Date());
            newFile.setDelFlag(FileFlagEnum.NORMAL.getFlag());

            fileInfoMapper.insert(newFile);

            // 如果是文件夹，需要递归复制子文件
            if (sourceFile.getFolderType() == 1) {
                copyFolderRecursive(share.getFileId(), share.getFromUserId(), newFile.getFileId(), userId);
            }

            logger.info("用户 {} 保存了好友 {} 分享的文件 {}", userId, share.getFromUserId(), share.getFileName());

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("文件保存成功");
            responseVO.setData(newFile.getFileId());
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("保存文件失败", e);
            throw new MyException("保存文件失败", FAIL_RES_CODE);
        }
    }

    /**
     * 递归复制文件夹及其子文件
     */
    private void copyFolderRecursive(String sourceFolderId, String sourceUserId, String targetFolderId, String targetUserId) {
        try {
            // 查询源文件夹下的所有文件
            LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileInfo::getUserId, sourceUserId)
                   .eq(FileInfo::getFilePid, sourceFolderId)
                   .eq(FileInfo::getDelFlag, 0);
            List<FileInfo> sourceFiles = fileInfoMapper.selectList(wrapper);

            for (FileInfo sourceFile : sourceFiles) {
                // 复制文件
                FileInfo newFile = new FileInfo();
                newFile.setFileId(StringUtils.getSerialNumber(15));
                newFile.setUserId(targetUserId);
                newFile.setFilePid(targetFolderId);
                newFile.setFileName(sourceFile.getFileName());
                newFile.setFileMd5(sourceFile.getFileMd5());
                newFile.setFileSize(sourceFile.getFileSize());
                newFile.setFileCover(sourceFile.getFileCover());
                newFile.setFilePath(sourceFile.getFilePath());
                newFile.setFolderType(sourceFile.getFolderType());
                newFile.setFileCategory(sourceFile.getFileCategory());
                newFile.setFileType(sourceFile.getFileType());
                newFile.setStatus(sourceFile.getStatus());
                newFile.setCreateTime(new Date());
                newFile.setLastUpdateTime(new Date());
                newFile.setDelFlag(0);

                fileInfoMapper.insert(newFile);

                // 如果是文件夹，递归复制
                if (sourceFile.getFolderType() == 1) {
                    copyFolderRecursive(sourceFile.getFileId(), sourceUserId, newFile.getFileId(), targetUserId);
                }
            }
        } catch (Exception e) {
            logger.error("递归复制文件夹失败", e);
        }
    }

    /**
     * 下载好友分享的文件
     */
    @Override
    public ResponseVO downloadFriendSharedFile(String userId, Long shareId) throws MyException {
        try {
            FriendFileShare share = friendFileShareMapper.selectById(shareId);
            if (share == null) {
                throw new MyException("分享不存在", FAIL_RES_CODE);
            }
            
            // 验证权限：分享者或接收者都可以下载
            if (!share.getFromUserId().equals(userId) && !share.getToUserId().equals(userId)) {
                throw new MyException("无权限下载此文件", FAIL_RES_CODE);
            }

            if (share.getStatus() != 1) {
                throw new MyException("分享已失效", FAIL_RES_CODE);
            }

            if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
                share.setStatus(2);
                friendFileShareMapper.updateById(share);
                throw new MyException("分享已过期", FAIL_RES_CODE);
            }

            // 查询原文件
            FileInfo sourceFile = fileInfoMapper.selectByFileIdAndUserId(share.getFileId(), share.getFromUserId());
            if (sourceFile == null) {
                throw new MyException("原文件不存在", FAIL_RES_CODE);
            }

            // 生成下载码（使用Redis存储，有效期5分钟）
            String downloadCode = StringUtils.getSerialNumber(50);
            String downloadKey = RedisUtils.buildKey("download:code", downloadCode);

            redisManager.setex(downloadKey, sourceFile.getFilePath(), 5 * 60);  // 5分钟有效期

            logger.info("用户 {} 创建了好友分享文件的下载链接: {}", userId, share.getFileName());

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setData(downloadCode);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取下载信息失败", e);
            throw new MyException("获取下载信息失败", FAIL_RES_CODE);
        }
    }

    /**
     * 预览好友分享的文件
     */
    @Override
    public ResponseVO previewFriendSharedFile(String userId, Long shareId) throws MyException {
        try {
            FriendFileShare share = friendFileShareMapper.selectById(shareId);
            if (share == null) {
                throw new MyException("分享不存在", FAIL_RES_CODE);
            }
            
            // 验证权限：分享者或接收者都可以预览
            if (!share.getFromUserId().equals(userId) && !share.getToUserId().equals(userId)) {
                throw new MyException("无权限预览此文件", FAIL_RES_CODE);
            }

            if (share.getStatus() != 1) {
                throw new MyException("分享已失效", FAIL_RES_CODE);
            }

            if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
                share.setStatus(2);
                friendFileShareMapper.updateById(share);
                throw new MyException("分享已过期", FAIL_RES_CODE);
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setData(share);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取预览信息失败", e);
            throw new MyException("获取预览信息失败", FAIL_RES_CODE);
        }
    }

    /**
     * 获取好友分享文件的内容（用于预览）
     * 复用FileService的逻辑
     */
    @Override
    public void getFriendSharedFile(String userId, Long shareId, String fileId, HttpServletResponse response) throws Exception {
        // 验证分享权限
        FriendFileShare share = validateShareAccess(userId, shareId);

        // 查询原文件
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, share.getFromUserId());
        if (fileInfo == null) {
            throw new MyException("文件不存在或已删除", FAIL_RES_CODE);
        }

        if (fileInfo.getStatus() != TRANS_SUCCEED.getStatus()) {
            throw new MyException("文件未转码完成", FAIL_RES_CODE);
        }

        // 使用MinioUtils获取文件
        String filePath = fileInfo.getFilePath();
        try {
            minioUtils.getFile(filePath, response);
            logger.info("用户 {} 预览了好友分享的文件: {}", userId, fileInfo.getFileName());
        } catch (Exception e) {
            logger.error("获取文件失败", e);
            throw new MyException("获取文件失败", FAIL_RES_CODE);
        }
    }

    /**
     * 获取好友分享视频的流信息（用于视频预览）
     * 复用FileService的视频预览逻辑
     */
    @Override
    public void getFriendSharedVideoInfo(String userId, Long shareId, String msg, HttpServletResponse response, HttpSession session) throws Exception {
        // 验证分享权限
        FriendFileShare share = validateShareAccess(userId, shareId);

        // 如果请求的是.ts文件
        if (msg.endsWith(".ts")) {
            String videoFolder = (String) session.getAttribute(SESSION_VIDEO_PATH_KEY);
            if (videoFolder == null) {
                throw new MyException("视频会话已过期，请重新加载", FAIL_RES_CODE);
            }
            String tsPath = videoFolder + "/" + msg;
            minioUtils.getFile(tsPath, response);
        } else {
            // msg是文件ID，需要获取m3u8文件
            FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(msg, share.getFromUserId());
            if (fileInfo == null) {
                throw new MyException("文件不存在或已删除", FAIL_RES_CODE);
            }

            if (fileInfo.getFileType() != VIDEO.getType()) {
                throw new MyException("不是视频文件", FAIL_RES_CODE);
            }

            if (fileInfo.getStatus() != TRANS_SUCCEED.getStatus()) {
                throw new MyException("视频转码未完成", FAIL_RES_CODE);
            }

            String filePath = fileInfo.getFilePath();
            String fileFolder = filePath.substring(0, filePath.lastIndexOf("/"));
            session.setAttribute(SESSION_VIDEO_PATH_KEY, fileFolder);

            String m3u8Path = fileFolder + "/" + FILE_DEFAULT_M3U8_NAME;
            minioUtils.getFile(m3u8Path, response);

            logger.info("用户 {} 预览了好友分享的视频: {}", userId, fileInfo.getFileName());
        }
    }

    /**
     * 获取好友分享文件的封面图片
     */
    @Override
    public void getFriendSharedImage(String userId, Long shareId, String coverPath, HttpServletResponse response) throws Exception {
        // 验证分享权限
        validateShareAccess(userId, shareId);

        // 使用MinioUtils获取图片
        try {
            minioUtils.getFile(coverPath, response);
        } catch (Exception e) {
            logger.error("获取封面图片失败", e);
            throw new MyException("获取封面图片失败", FAIL_RES_CODE);
        }
    }

    /**
     * 创建好友分享文件的下载链接（用于预览下载）
     */
    @Override
    public ResponseVO createFriendSharedDownloadUrl(String userId, Long shareId, String fileId) throws MyException {
        try {
            // 验证分享权限
            FriendFileShare share = validateShareAccess(userId, shareId);

            // 查询原文件
            FileInfo sourceFile = fileInfoMapper.selectByFileIdAndUserId(fileId, share.getFromUserId());
            if (sourceFile == null) {
                throw new MyException("原文件不存在", FAIL_RES_CODE);
            }

            // 生成下载码（使用Redis存储，有效期5分钟）
            String downloadCode = StringUtils.getSerialNumber(50);
            String downloadKey = RedisUtils.buildKey("download:code", downloadCode);

            redisManager.setex(downloadKey, sourceFile.getFilePath(), 5 * 60);

            logger.info("用户 {} 创建了好友分享文件的下载链接: {}", userId, sourceFile.getFileName());

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setData(downloadCode);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("创建下载链接失败", e);
            throw new MyException("创建下载链接失败", FAIL_RES_CODE);
        }
    }

    /**
     * 验证分享访问权限的通用方法
     * 分享者和接收者都有权限访问
     */
    private FriendFileShare validateShareAccess(String userId, Long shareId) throws MyException {
        FriendFileShare share = friendFileShareMapper.selectById(shareId);
        if (share == null) {
            throw new MyException("分享不存在", FAIL_RES_CODE);
        }
        
        // 验证权限：分享者或接收者都可以访问
        if (!share.getFromUserId().equals(userId) && !share.getToUserId().equals(userId)) {
            throw new MyException("无权限访问此分享", FAIL_RES_CODE);
        }

        if (share.getStatus() != 1) {
            throw new MyException("分享已失效", FAIL_RES_CODE);
        }

        if (share.getExpireTime() != null && share.getExpireTime().before(new Date())) {
            share.setStatus(2);
            friendFileShareMapper.updateById(share);
            throw new MyException("分享已过期", FAIL_RES_CODE);
        }

        return share;
    }
}
