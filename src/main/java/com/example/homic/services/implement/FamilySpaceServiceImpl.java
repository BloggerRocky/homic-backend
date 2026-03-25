package com.example.homic.services.implement;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.homic.config.RedisManager;
import com.example.homic.config.properties.AppProperties;
import com.example.homic.constants.enums.FileTypeEnum;
import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.frontEnd.UploadDTO;
import com.example.homic.dto.rabbitMQ.MergeMessageDTO;
import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.FamilyMapper;
import com.example.homic.mapper.FamilyMemberMapper;
import com.example.homic.mapper.FileInfoMapper;
import com.example.homic.mapper.UserInfoMapper;
import com.example.homic.model.Family;
import com.example.homic.model.FamilyMember;
import com.example.homic.model.UserInfo;
import com.example.homic.model.file.FileInfo;
import com.example.homic.services.FamilySpaceService;
import com.example.homic.services.PermissionService;
import com.example.homic.utils.MinioUtils;
import com.example.homic.utils.RedisUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.*;
import io.minio.GetObjectResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.homic.constants.CodeConstants.*;
import static com.example.homic.constants.NormalConstants.*;
import static com.example.homic.constants.enums.DatePatternEnum.YEAR_MONTH_DAY;
import static com.example.homic.constants.enums.FileFlagEnum.NORMAL;
import static com.example.homic.constants.enums.FileFlagEnum.RECYCLE;
import static com.example.homic.constants.enums.FileStatusEnum.*;
import static com.example.homic.constants.enums.UploadStatusEnum.*;

@Service
public class FamilySpaceServiceImpl implements FamilySpaceService {

    private static final Logger logger = LoggerFactory.getLogger(FamilySpaceServiceImpl.class);

    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private FamilyMapper familyMapper;
    @Autowired
    private FamilyMemberMapper familyMemberMapper;
    @Resource
    private PermissionService permissionService;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisManager redisManager;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MinioUtils minioUtils;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AppProperties appProperties;

    // ===================== 权限校验 =====================

    /**
     * 校验用户是否属于指定家庭，返回成员信息
     */
    private FamilyMember validateFamilyMember(String userId, String familyId) throws MyException {
        if (familyId == null || familyId.isEmpty()) {
            throw new MyException("家庭ID不能为空", FAIL_RES_CODE);
        }
        LambdaQueryWrapper<FamilyMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FamilyMember::getUserId, userId);
        lqw.eq(FamilyMember::getFamilyId, familyId);
        FamilyMember member = familyMemberMapper.selectOne(lqw);
        if (member == null) {
            throw new MyException("您不属于该家庭", FAIL_RES_CODE);
        }
        return member;
    }

    // ===================== 家庭空间管理 =====================

    /**
     * 刷新家庭空间使用量（数据库 + Redis缓存）
     */
    @Override
    public void refreshFamilySpace(String familyId) {
        Long useSpace = fileInfoMapper.getSizeByFamilyId(familyId);
        useSpace = useSpace == null ? 0 : useSpace;
        // 更新Redis缓存
        RedisUseSpaceDTO spaceDTO = redisManager.get(RedisUtils.getFamilySpaceKey(familyId), RedisUseSpaceDTO.class);
        if (spaceDTO != null) {
            spaceDTO.setUseSpace(useSpace);
            redisManager.setex(RedisUtils.getFamilySpaceKey(familyId), spaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
        }
        // 更新数据库
        Family family = new Family();
        family.setFamilyId(familyId);
        family.setUseSpace(useSpace);
        familyMapper.updateById(family);
    }

    /**
     * 获取家庭空间信息（优先从Redis读取，未命中则从DB加载并缓存）
     */
    private RedisUseSpaceDTO getFamilySpaceInfo(String familyId) {
        RedisUseSpaceDTO spaceDTO = redisManager.get(RedisUtils.getFamilySpaceKey(familyId), RedisUseSpaceDTO.class);
        if (spaceDTO == null) {
            Family family = familyMapper.selectById(familyId);
            if (family == null) return null;
            spaceDTO = new RedisUseSpaceDTO();
            spaceDTO.setUseSpace(family.getUseSpace() == null ? 0 : family.getUseSpace());
            spaceDTO.setTotalSpace(family.getTotalSpace() == null ? 1073741824L : family.getTotalSpace());
            redisManager.setex(RedisUtils.getFamilySpaceKey(familyId), spaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
        }
        return spaceDTO;
    }

    // ===================== 文件列表 =====================

    @Override
    public ResponseVO loadDataList(QueryInfoDTO queryInfoDTO, String userId, String familyId) throws MyException {
        // 校验用户是否属于该家庭
        validateFamilyMember(userId, familyId);

        // 获取用户信息，判断是否为关怀用户
        UserInfo userInfo = userInfoMapper.selectById(userId);
        boolean isCareUser = userInfo != null && userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1;

        // 家庭空间：按 belongingHome 查询，不按 userId 过滤
        queryInfoDTO.setDelFlag(NORMAL.getFlag());
        queryInfoDTO.setBelongingHome(familyId);
        queryInfoDTO.setQueryNickName(true);

        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        String categoryDesc = queryInfoDTO.getCategory();
        if (categoryDesc != null && !categoryDesc.equals("all")) {
            Integer category = com.example.homic.constants.enums.FileCategoryEnum.getCategoryByDesc(categoryDesc);
            fileInfoLqw.eq(FileInfo::getFileCategory, category);
        } else {
            String filePid = queryInfoDTO.getFilePid().equals("") ? "0" : queryInfoDTO.getFilePid();
            fileInfoLqw.eq(FileInfo::getFilePid, filePid);
        }
        if (queryInfoDTO.getFileNameFuzzy() != null && !queryInfoDTO.getFileNameFuzzy().equals(""))
            fileInfoLqw.like(FileInfo::getFileName, queryInfoDTO.getFileNameFuzzy());
        fileInfoLqw.eq(FileInfo::getDelFlag, queryInfoDTO.getDelFlag());
        fileInfoLqw.eq(FileInfo::getBelongingHome, familyId);

        // 如果是关怀用户，只显示对关怀用户可见的文件
        if (isCareUser) {
            fileInfoLqw.eq(FileInfo::getVisibleToCare, 1);
        }

        int pageSize = queryInfoDTO.getPageSize() == null ? DEFAULT_PAGE_SIZE : queryInfoDTO.getPageSize();
        int pageNo = queryInfoDTO.getPageNo() == null ? 1 : queryInfoDTO.getPageNo();
        IPage page = new Page<>(pageNo, pageSize);
        fileInfoMapper.selectPage(page, fileInfoLqw);
        PageResultVO<FileInfo> pageResultVO = new PageResultVO<>(page, FileInfo.class);
        // 查询昵称
        List<FileInfo> fileInfoList = pageResultVO.getList();
        for (FileInfo fileInfo : fileInfoList) {
            fileInfo.setNickName(userInfoMapper.selectByPrimaryKey(fileInfo.getUserId()).getNickName());
        }
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取家庭文件列表成功");
        responseVO.setData(pageResultVO);
        return responseVO;
    }

    // ===================== 上传文件 =====================

    @Override
    @Transactional
    public UploadVO uploadFile(UploadDTO uploadDTO, String userId, String familyId) throws Exception {
        // 检查用户是否属于该家庭
        validateFamilyMember(userId, familyId);

        // 检查上传权限
        if (!permissionService.canUpload(userId, familyId)) {
            throw new MyException("无权限上传文件", FAIL_RES_CODE);
        }

        // 设置所属家庭
        uploadDTO.setBelongingHome(familyId);

        // 获取用户信息，判断是否为关怀用户
        UserInfo userInfo = userInfoMapper.selectById(userId);
        boolean isCareUser = userInfo != null && userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1;

        // 关怀用户上传的文件默认对关怀用户可见
        if (isCareUser) {
            uploadDTO.setVisibleToCare(1);
        } else {
            uploadDTO.setVisibleToCare(0);
        }

        String fileId = uploadDTO.getFileId();
        if (StringUtils.isEmpty(fileId)) {
            fileId = StringUtils.getSerialNumber(FILE_ID_LENGTH);
            uploadDTO.setFileId(fileId);
            redisManager.setex(RedisUtils.getTempSizeKey(userId, fileId), "0", REDIS_TEMP_EXPIRE_TIME);
            FileInfo fileInfo = fileInfoMapper.selectOneByFileMd5(uploadDTO.getFileMd5());
            if (fileInfo != null && fileInfo.getStatus() == TRANS_SUCCEED.getStatus())
                return familyUploadBySecond(uploadDTO, userId, familyId, fileInfo);
            else
                return familyUploadBySplit(uploadDTO, userId, familyId);
        } else {
            return familyUploadBySplit(uploadDTO, userId, familyId);
        }
    }

    /**
     * 秒传（家庭空间版本）：校验家庭空间容量
     */
    private UploadVO familyUploadBySecond(UploadDTO uploadDTO, String userId, String familyId, FileInfo fileInfo) throws Exception {
        RedisUseSpaceDTO spaceDTO = getFamilySpaceInfo(familyId);
        Long useSpace = spaceDTO.getUseSpace();
        Long totalSpace = spaceDTO.getTotalSpace();
        String fileId = uploadDTO.getFileId();
        if (useSpace + fileInfo.getFileSize() > totalSpace)
            throw new MyException("家庭空间不足", NO_SPACE_RES_CODE);

        if (fileInfo.getFilePid().equals(uploadDTO.getFilePid())
                && fileInfo.getBelongingHome() != null
                && fileInfo.getBelongingHome().equals(familyId)
                && fileInfo.getFileName().equals(uploadDTO.getFileName()))
            throw new MyException("文件已存在,请勿重复上传", ERROR_RES_CODE);

        fileInfo.setFileId(fileId);
        fileInfo.setUserId(userId);
        fileInfo.setFilePid(uploadDTO.getFilePid());
        fileInfo.setFileName(uploadDTO.getFileName());
        fileInfo.setBelongingHome(familyId);
        fileInfo.setVisibleToCare(uploadDTO.getVisibleToCare());
        Date date = new Date();
        fileInfo.setCreateTime(date);
        fileInfo.setLastUpdateTime(date);
        fileInfo.setDelFlag(NORMAL.getFlag());
        fileInfoMapper.insertSelective(fileInfo);
        // 刷新家庭空间
        try {
            refreshFamilySpace(familyId);
        } catch (Exception e) {
            logger.error("刷新家庭空间信息失败", e);
            throw new MyException("刷新家庭空间信息失败", 404);
        }
        UploadVO uploadVO = new UploadVO();
        uploadVO.setFileId(fileId);
        uploadVO.setStatus(UPLOAD_SECONDS.getStatus());
        return uploadVO;
    }

    /**
     * 分片上传（家庭空间版本）：校验家庭空间容量
     */
    private UploadVO familyUploadBySplit(UploadDTO uploadDTO, String userId, String familyId) throws Exception {
        if (uploadDTO.getChunks() > MAX_CHUNK_SIZE)
            throw new MyException("文件过大", ERROR_RES_CODE);
        String fileId = uploadDTO.getFileId();
        String tempSizeKey = RedisUtils.getTempSizeKey(userId, fileId);
        RedisUseSpaceDTO spaceDTO = getFamilySpaceInfo(familyId);
        Long tempSize = Long.parseLong(redisManager.get(tempSizeKey, String.class));
        if (tempSize + uploadDTO.getFile().getSize() + spaceDTO.getUseSpace() > spaceDTO.getTotalSpace()) {
            redisManager.delete(tempSizeKey);
            minioUtils.deleteFolder(FILE_TEMP_PATH + userId + "/" + fileId);
            throw new MyException("家庭空间不足", NO_SPACE_RES_CODE);
        } else {
            redisManager.setex(tempSizeKey, String.valueOf(tempSize + uploadDTO.getFile().getSize()), REDIS_TEMP_EXPIRE_TIME);
        }
        String tempFilePath = FILE_TEMP_PATH + userId + "/" + fileId + "/" + uploadDTO.getChunkIndex();
        try {
            minioUtils.saveMultipartFile(tempFilePath, uploadDTO.getFile());
        } catch (MyException e) {
            logger.error(e.msg);
            throw new MyException("文件上传失败", FAIL_RES_CODE);
        }
        String status = null;
        if (uploadDTO.getChunks() - 1 == uploadDTO.getChunkIndex()) {
            status = UPLOAD_FINISH.getStatus();
            familyUnionFile(uploadDTO, userId, familyId);
        } else {
            status = UPLOADING.getStatus();
        }
        UploadVO uploadVO = new UploadVO();
        uploadVO.setFileId(fileId);
        uploadVO.setStatus(status);
        return uploadVO;
    }

    /**
     * 合并文件（家庭空间版本）：刷新家庭空间而非个人空间
     */
    @Transactional
    public void familyUnionFile(UploadDTO uploadDTO, String userId, String familyId) throws Exception {
        try {
            String suffix = uploadDTO.getFileName().substring(uploadDTO.getFileName().lastIndexOf("."));
            FileTypeEnum typeEnum = FileTypeEnum.getTypeBySuffix(suffix);
            Date date = new Date();
            String dateStr = StringUtils.formatDate(date, YEAR_MONTH_DAY.getPattern());
            String fileId = uploadDTO.getFileId();
            String fileSizeStr = redisManager.get(RedisUtils.getTempSizeKey(userId, fileId), String.class);
            Long fileSize = Long.parseLong(fileSizeStr);
            String fileName = uploadDTO.getFileName();
            String filePath = FILE_ROOT_PATH + dateStr + "/" + fileId + "/" + fileName;
            String coverPath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + FILE_DEFAULT_COVER_NAME;
            int fileType = typeEnum.getType();
            int fileCategory = typeEnum.getCategory().getCategory();

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(uploadDTO.getFileId());
            fileInfo.setUserId(userId);
            fileInfo.setFileMd5(uploadDTO.getFileMd5());
            fileInfo.setFilePid(uploadDTO.getFilePid());
            fileInfo.setFileName(fileName);
            fileInfo.setFileSize(fileSize);
            fileInfo.setFilePath(filePath);
            fileInfo.setFileCover(coverPath);
            fileInfo.setCreateTime(date);
            fileInfo.setLastUpdateTime(date);
            fileInfo.setFolderType(FOLDER_TYPE_FILE);
            fileInfo.setFileType(fileType);
            fileInfo.setStatus(TRANSFERRING.getStatus());
            fileInfo.setFileCategory(fileCategory);
            fileInfo.setBelongingHome(familyId);
            fileInfo.setVisibleToCare(uploadDTO.getVisibleToCare());
            fileInfo.setDelFlag(NORMAL.getFlag());
            fileInfoMapper.insertSelective(fileInfo);
            // 刷新家庭空间（而非个人空间）
            refreshFamilySpace(familyId);
            // 发送合并任务到RabbitMQ
            String folderPath = filePath.substring(0, filePath.lastIndexOf("/"));
            MergeMessageDTO mergeMessageDTO = new MergeMessageDTO(uploadDTO, folderPath, userId);
            String mergeString = JSON.toJSONString(mergeMessageDTO, SerializerFeature.IgnoreErrorGetter);
            rabbitTemplate.convertAndSend(mergeMessageDTO.EXCHANGE_NAME, mergeMessageDTO.ROUTING_KEY, mergeString);
        } catch (Exception e) {
            logger.error("家庭文件信息保存失败", e);
            throw new MyException("文件上传失败", FAIL_RES_CODE);
        } finally {
            redisManager.delete(RedisUtils.getTempSizeKey(userId, uploadDTO.getFileId()));
        }
    }

    // ===================== 新建文件夹 =====================

    @Override
    public FileInfoVO newFolder(String filePid, String fileName, String userId, String familyId) throws MyException {
        // 检查上传权限（创建文件夹属于上传类操作）
        if (!permissionService.canUpload(userId, familyId)) {
            throw new MyException("无权限创建文件夹", FAIL_RES_CODE);
        }

        // 查重
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getFilePid, filePid);
        fileInfoLqw.eq(FileInfo::getFileName, fileName);
        fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
        fileInfoLqw.eq(FileInfo::getFolderType, FOLDER_TYPE_FOLDER);
        fileInfoLqw.eq(FileInfo::getBelongingHome, familyId);
        try {
            if (fileInfoMapper.selectCount(fileInfoLqw) > 0) {
                return null;
            }
            FileInfo fileInfo = new FileInfo();
            String fileId = StringUtils.getSerialNumber(FILE_ID_LENGTH);
            fileInfo.setFileId(fileId);
            fileInfo.setUserId(userId);
            fileInfo.setFilePid(filePid);
            fileInfo.setFileName(fileName);
            fileInfo.setBelongingHome(familyId);

            // 获取用户信息，判断是否为关怀用户
            UserInfo userInfo = userInfoMapper.selectById(userId);
            boolean isCareUser = userInfo != null && userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1;

            // 关怀用户创建的文件夹默认对关怀用户可见
            fileInfo.setVisibleToCare(isCareUser ? 1 : 0);

            Date date = new Date();
            fileInfo.setCreateTime(date);
            fileInfo.setLastUpdateTime(date);
            fileInfo.setFileCover(FILE_SYSTEM_PATH + DEFAULT_FOLDER_ICON_NAME);
            fileInfo.setFolderType(FOLDER_TYPE_FOLDER);
            fileInfo.setDelFlag(NORMAL.getFlag());
            fileInfo.setStatus(TRANS_SUCCEED.getStatus());
            fileInfoMapper.insertSelective(fileInfo);
            return modelMapper.map(fileInfo, FileInfoVO.class);
        } catch (Exception e) {
            logger.error("创建家庭文件夹时发生异常", e);
            throw new MyException("创建文件夹时发生异常", FAIL_RES_CODE);
        }
    }

    /**
     * 获取家庭空间使用情况
     */
    @Override
    public ResponseVO getFamilySpaceUsage(String userId, String familyId) throws MyException {
        // 检查查看权限
        if (!permissionService.canView(userId, familyId)) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限查看文件");
        }

        // 校验用户是否属于该家庭
        validateFamilyMember(userId, familyId);
        // 获取家庭空间信息
        RedisUseSpaceDTO spaceDTO = getFamilySpaceInfo(familyId);
        if (spaceDTO == null) {
            throw new MyException("家庭不存在", FAIL_RES_CODE);
        }
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取家庭空间使用情况成功");
        responseVO.setData(spaceDTO);
        return responseVO;
    }

    /**
     * 加载家庭空间所有文件夹（用于移动功能）
     */
    @Override
    public ResponseVO loadAllFolder(String filePid, String currentFileIds, String userId, String familyId) throws MyException {
        // 检查查看权限
        if (!permissionService.canView(userId, familyId)) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限查看文件");
        }

        // 校验用户是否属于该家庭
        validateFamilyMember(userId, familyId);

        try {
            LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
            fileInfoLqw.eq(FileInfo::getFilePid, filePid);
            fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
            fileInfoLqw.eq(FileInfo::getFolderType, FOLDER_TYPE_FOLDER);
            fileInfoLqw.eq(FileInfo::getBelongingHome, familyId);

            // 如果有需要排除的文件ID，添加排除条件
            if (currentFileIds != null && !currentFileIds.isEmpty()) {
                String[] idArray = currentFileIds.split(",");
                fileInfoLqw.notIn(FileInfo::getFileId, idArray);
            }

            List<FileInfo> fileInfos = fileInfoMapper.selectList(fileInfoLqw);
            List<FileInfoVO> fileInfoVOs = fileInfos.stream()
                    .map(fileInfo -> modelMapper.map(fileInfo, FileInfoVO.class))
                    .collect(Collectors.toList());
            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取文件夹列表成功");
            responseVO.setData(fileInfoVOs);
            return responseVO;
        } catch (Exception e) {
            logger.error("获取家庭空间文件夹列表失败", e);
            throw new MyException("获取文件夹信息失败", FAIL_RES_CODE);
        }
    }

    /**
     * 删除家庭空间文件
     */
    @Override
    @Transactional
    public ResponseVO deleteFamilyFiles(String fileIds, String userId, String familyId) throws MyException {
        // 检查删除权限
        if (!permissionService.canDelete(userId, familyId)) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限删除文件");
        }

        // 校验用户是否属于该家庭
        validateFamilyMember(userId, familyId);

        try {
            // 将所有待删除文件的状态改成回收站
            List<String> fileIdsList = List.of(fileIds.split(","));
            LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
            fileInfoLqw.in(FileInfo::getFileId, fileIdsList);
            fileInfoLqw.eq(FileInfo::getBelongingHome, familyId);
            fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());

            FileInfo fileInfo = new FileInfo();
            fileInfo.setRecoveryTime(new Date());
            fileInfo.setDelFlag(RECYCLE.getFlag());
            fileInfoMapper.update(fileInfo, fileInfoLqw);

            // 刷新家庭空间使用量
            refreshFamilySpace(familyId);

            return new ResponseVO(SUCCESS_RES_STATUS, "文件删除成功");
        } catch (Exception e) {
            logger.error("删除家庭空间文件失败", e);
            throw new MyException("删除文件失败", FAIL_RES_CODE);
        }
    }

    /**
     * 重命名家庭空间文件
     */
    @Override
    @Transactional
    public ResponseVO renameFile(String fileId, String fileName, String userId, String familyId) throws MyException {
        // 检查修改权限
        if (!permissionService.canModify(userId, familyId)) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限重命名文件");
        }

        // 校验用户是否属于该家庭
        validateFamilyMember(userId, familyId);

        // 查询文件
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getFileId, fileId);
        fileInfoLqw.eq(FileInfo::getBelongingHome, familyId);
        fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
        FileInfo dbFile = fileInfoMapper.selectOne(fileInfoLqw);

        if (dbFile == null) {
            throw new MyException("文件不存在", FAIL_RES_CODE);
        }

        // 检查文件名是否包含斜杠
        if (fileName.contains("/")) {
            throw new MyException("文件名不能包含斜杠", FAIL_RES_CODE);
        }

        // 获取原始文件名后缀
        String suffix = dbFile.getFileName().substring(dbFile.getFileName().lastIndexOf("."));
        String newFileName = fileName + suffix;

        // 检查是否有同名文件
        LambdaQueryWrapper<FileInfo> checkLqw = new LambdaQueryWrapper<>();
        checkLqw.eq(FileInfo::getFilePid, dbFile.getFilePid());
        checkLqw.eq(FileInfo::getFileName, newFileName);
        checkLqw.eq(FileInfo::getBelongingHome, familyId);
        checkLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
        checkLqw.ne(FileInfo::getFileId, fileId);

        if (fileInfoMapper.selectCount(checkLqw) > 0) {
            throw new MyException("该目录下已存在同名文件", FAIL_RES_CODE);
        }

        // 更新文件名
        FileInfo updateInfo = new FileInfo();
        updateInfo.setFileName(newFileName);
        updateInfo.setLastUpdateTime(new Date());
        fileInfoMapper.update(updateInfo, fileInfoLqw);

        // 返回更新后的文件信息
        dbFile.setFileName(newFileName);
        FileInfoVO fileInfoVO = modelMapper.map(dbFile, FileInfoVO.class);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "重命名成功");
        responseVO.setData(fileInfoVO);
        return responseVO;
    }

    /**
     * 移动家庭空间文件
     */
    @Override
    @Transactional
    public ResponseVO changeFileFolder(String fileIds, String filePid, String userId, String familyId) throws MyException {
        // 检查修改权限
        if (!permissionService.canModify(userId, familyId)) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限移动文件");
        }

        // 校验用户是否属于该家庭
        validateFamilyMember(userId, familyId);

        try {
            String[] fileIdArray = fileIds.split(",");
            for (String fileId : fileIdArray) {
                // 查询文件
                LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
                fileInfoLqw.eq(FileInfo::getFileId, fileId);
                fileInfoLqw.eq(FileInfo::getBelongingHome, familyId);
                fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
                FileInfo dbFile = fileInfoMapper.selectOne(fileInfoLqw);

                if (dbFile == null) {
                    continue; // 跳过不存在的文件
                }

                // 检查目标文件夹是否有效
                if (!"0".equals(filePid)) {
                    LambdaQueryWrapper<FileInfo> folderLqw = new LambdaQueryWrapper<>();
                    folderLqw.eq(FileInfo::getFileId, filePid);
                    folderLqw.eq(FileInfo::getBelongingHome, familyId);
                    folderLqw.eq(FileInfo::getFolderType, FOLDER_TYPE_FOLDER);
                    folderLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
                    if (fileInfoMapper.selectCount(folderLqw) == 0) {
                        throw new MyException("目标文件夹不存在", FAIL_RES_CODE);
                    }
                }

                // 检查是否有同名文件
                LambdaQueryWrapper<FileInfo> checkLqw = new LambdaQueryWrapper<>();
                checkLqw.eq(FileInfo::getFilePid, filePid);
                checkLqw.eq(FileInfo::getFileName, dbFile.getFileName());
                checkLqw.eq(FileInfo::getBelongingHome, familyId);
                checkLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
                checkLqw.ne(FileInfo::getFileId, fileId);

                if (fileInfoMapper.selectCount(checkLqw) > 0) {
                    throw new MyException("目标目录下已存在同名文件: " + dbFile.getFileName(), FAIL_RES_CODE);
                }

                // 更新父文件夹ID
                FileInfo updateInfo = new FileInfo();
                updateInfo.setFilePid(filePid);
                updateInfo.setLastUpdateTime(new Date());
                LambdaQueryWrapper<FileInfo> updateLqw = new LambdaQueryWrapper<>();
                updateLqw.eq(FileInfo::getFileId, fileId);
                updateLqw.eq(FileInfo::getBelongingHome, familyId);
                fileInfoMapper.update(updateInfo, updateLqw);
            }

            return new ResponseVO(SUCCESS_RES_STATUS, "移动成功");
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("移动家庭空间文件失败", e);
            throw new MyException("移动文件失败", FAIL_RES_CODE);
        }
    }

    /**
     * 更新文件对关怀用户的可见性
     */
    @Override
    @Transactional
    public ResponseVO updateFileVisibleToCare(String fileId, Integer visibleToCare, String userId, String familyId) throws MyException {
        // 校验用户是否属于该家庭
        validateFamilyMember(userId, familyId);

        // 获取用户在家庭中的角色
        FamilyMember member = validateFamilyMember(userId, familyId);
        Integer userRole = member.getRole();

        // 获取家庭信息，检查是否为创建者
        LambdaQueryWrapper<Family> familyLqw = new LambdaQueryWrapper<>();
        familyLqw.eq(Family::getFamilyId, familyId);
        Family family = familyMapper.selectOne(familyLqw);
        boolean isCreator = family != null && family.getCreatorId().equals(userId);

        // 权限检查：
        // 1. 创建者硬编码拥有权限
        // 2. 管理员需要检查是否有"关怀用户可见性管理"权限
        // 3. 普通用户和关怀账号不能拥有该权限
        if (!isCreator) {
            if (userRole == null || userRole == 2) {
                // 普通用户无权限
                return new ResponseVO(FAIL_RES_STATUS, "无权限修改文件可见性");
            } else if (userRole == 1) {
                // 管理员需要检查权限
                if (!permissionService.hasPermission(userId, permissionService.PERMISSION_CARE_VISIBILITY_MANAGE, familyId)) {
                    return new ResponseVO(FAIL_RES_STATUS, "无权限修改文件可见性，请联系管理员分配权限");
                }
            }
        }

        try {
            // 查询文件
            LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
            fileInfoLqw.eq(FileInfo::getFileId, fileId);
            fileInfoLqw.eq(FileInfo::getBelongingHome, familyId);
            fileInfoLqw.eq(FileInfo::getDelFlag, NORMAL.getFlag());
            FileInfo dbFile = fileInfoMapper.selectOne(fileInfoLqw);

            if (dbFile == null) {
                return new ResponseVO(FAIL_RES_STATUS, "文件不存在");
            }

            // 更新可见性
            LambdaUpdateWrapper<FileInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FileInfo::getFileId, fileId)
                        .eq(FileInfo::getBelongingHome, familyId)
                        .set(FileInfo::getVisibleToCare, visibleToCare);

            int result = fileInfoMapper.update(null, updateWrapper);

            if (result <= 0) {
                return new ResponseVO(FAIL_RES_STATUS, "更新失败");
            }

            return new ResponseVO(SUCCESS_RES_STATUS, "更新成功");
        } catch (Exception e) {
            logger.error("更新文件可见性失败", e);
            throw new MyException("更新失败", FAIL_RES_CODE);
        }
    }
}
