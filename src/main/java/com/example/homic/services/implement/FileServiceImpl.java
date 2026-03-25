package com.example.homic.services.implement;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.homic.config.properties.AppProperties;
import com.example.homic.constants.enums.FileCategoryEnum;
import com.example.homic.constants.enums.FileTypeEnum;
import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.rabbitMQ.MergeMessageDTO;
import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.dto.frontEnd.UploadDTO;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.FileInfoMapper;
import com.example.homic.mapper.FamilyMemberMapper;
import com.example.homic.mapper.UserInfoMapper;
import com.example.homic.model.file.FileInfo;
import com.example.homic.services.FileService;
import com.example.homic.utils.*;
import com.example.homic.vo.*;
import io.minio.GetObjectResponse;
import org.modelmapper.ModelMapper;

import static com.example.homic.constants.CodeConstants.*;
import static com.example.homic.constants.enums.DatePatternEnum.YEAR_MONTH_DAY;
import static com.example.homic.constants.enums.FileFlagEnum.NORMAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import static com.example.homic.constants.NormalConstants.*;
import static com.example.homic.constants.enums.FileStatusEnum.*;
import static com.example.homic.constants.enums.UploadStatusEnum.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.18:55
 * 项目名：homic
 */
@Service
public class FileServiceImpl extends CommonServiceImpl implements FileService  {
    Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    @Autowired
    AppProperties appProperties;
    @Autowired
    com.example.homic.config.RedisManager redisManager;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    FamilyMemberMapper familyMemberMapper;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    MinioUtils minioUtils;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Override
    public ResponseVO loadDataList(QueryInfoDTO queryInfoDTO) {
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        // @T确定文件的粗分类,文件分类 和 文件夹查找是对立关系，如果分类，则不能根据父文件查询。
        String categoryDesc = queryInfoDTO.getCategory();
        if( categoryDesc != null && !categoryDesc.equals("all"))
        {
            //通过前端分类描述desc获得category对应值
            Integer category = FileCategoryEnum.getCategoryByDesc(categoryDesc);
            fileInfoLqw.eq(FileInfo::getFileCategory,category);
        }
        else
        {
            //如果没有父文件输入，则默认查找所有文件
            String filePid = queryInfoDTO.getFilePid().equals("")?"0":queryInfoDTO.getFilePid();
                fileInfoLqw.eq(FileInfo::getFilePid,filePid);
        }
        if(queryInfoDTO.getFileId() != null && !queryInfoDTO.getFileId().equals(""))
            fileInfoLqw.eq(FileInfo::getFileId,queryInfoDTO.getFileId());
        if(queryInfoDTO.getUserId() != null && !queryInfoDTO.getUserId().equals(""))
            fileInfoLqw.eq(FileInfo::getUserId,queryInfoDTO.getUserId());//根据userId查询文件
        if(queryInfoDTO.getFileNameFuzzy() != null && !queryInfoDTO.getFileNameFuzzy().equals(""))
            fileInfoLqw.like(FileInfo::getFileName,queryInfoDTO.getFileNameFuzzy());//模糊查询文件名
        if(queryInfoDTO.getDelFlag() != -1)
            fileInfoLqw.eq(FileInfo::getDelFlag,queryInfoDTO.getDelFlag());//查询对应状态文件，其中-1表示不进行筛选
        // 区分个人文件和家庭文件
        if(queryInfoDTO.isFilterPersonal()) {
            fileInfoLqw.isNull(FileInfo::getBelongingHome);//仅查个人文件
        } else if(queryInfoDTO.getBelongingHome() != null && !queryInfoDTO.getBelongingHome().equals("")) {
            fileInfoLqw.eq(FileInfo::getBelongingHome, queryInfoDTO.getBelongingHome());//查家庭文件
        }
        int pageSize = queryInfoDTO.getPageSize()==null?DEFAULT_PAGE_SIZE:queryInfoDTO.getPageSize();
        int pageNo = queryInfoDTO.getPageNo()==null?1:queryInfoDTO.getPageNo();
        //查询分页记录
        IPage page=new Page<>(pageNo,pageSize);
        fileInfoMapper.selectPage(page,fileInfoLqw);
        //通过page快速生成页面 返回体对象实例
        PageResultVO<FileInfo> pageResultVO = new PageResultVO<>(page, FileInfo.class);
        if(queryInfoDTO.isQueryNickName())
        {
            List<FileInfo> fileInfoList = pageResultVO.getList();
            for(FileInfo fileInfo:fileInfoList)
            {
                //如果要查询用户昵称，就插入用户昵称
                fileInfo.setNickName(userInfoMapper.selectByPrimaryKey(fileInfo.getUserId()).getNickName());
            }
        }
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取文件列表成功");
        responseVO.setData(pageResultVO);
        return responseVO;
    }
    //装填分页信息


    /**
     * 上传文件（分片上传)
     * @param uploadDTO
     * @param userId
     * @return
     * @throws MyException
     */
    @Transactional
    @Override
    public UploadVO uploadFile(UploadDTO uploadDTO, String userId) throws Exception {
        //获取fileId，如果为空，则说明该文件是分片的第一个文件，
        String fileId = uploadDTO.getFileId();
        //如果fileId为空，则说明本次上传是该文件是第一次分片上传
        if(StringUtils.isEmpty(fileId))
        {
            //获取一个新的fileId
            fileId = StringUtils.getSerialNumber(FILE_ID_LENGTH);
            uploadDTO.setFileId(fileId);
            redisManager.setex(RedisUtils.getTempSizeKey(userId, fileId), "0", REDIS_TEMP_EXPIRE_TIME);
            //根据md5值查找数据库中是否含有相同的转码成功的文件
            FileInfo fileInfo = fileInfoMapper.selectOneByFileMd5(uploadDTO.getFileMd5());
            if(fileInfo != null && fileInfo.getStatus() ==  TRANS_SUCCEED.getStatus())
                return uploadBySecond(uploadDTO, userId, fileInfo);//执行秒传
            else
                return uploadBySplit(uploadDTO, userId);//执行分片上传
        }
        else
            return uploadBySplit(uploadDTO, userId);//执行分片上传

    }
    //执行秒传，上传时有现存文件
    private UploadVO uploadBySecond(UploadDTO uploadDTO, String userId, FileInfo fileInfo) throws Exception {
        RedisUseSpaceDTO redisUseSpaceDTO = redisManager.get(RedisUtils.getUserSpaceKey(userId), RedisUseSpaceDTO.class);
        Long useSpace = redisUseSpaceDTO.getUseSpace();
        Long totalSpace = redisUseSpaceDTO.getTotalSpace();
        String fileId = uploadDTO.getFileId();
        //查取内存，校验内存空间是否足够
        if(useSpace+ fileInfo.getFileSize()>totalSpace)
            throw new MyException("内存空间不足",NO_SPACE_RES_CODE);
        else
            {
                if(fileInfo.getFilePid() .equals( uploadDTO.getFilePid())
                        && fileInfo.getUserId().equals(userId)
                        && fileInfo.getFileName().equals(uploadDTO.getFileName()))
                    throw new MyException("文件已存在,请勿重复上传",ERROR_RES_CODE);
                //添加文件信息
                fileInfo.setFileId(fileId);
                fileInfo.setUserId(userId);
                fileInfo.setFilePid(uploadDTO.getFilePid());
                fileInfo.setFileName(uploadDTO.getFileName());
                Date date = new Date();
                fileInfo.setCreateTime(date);
                fileInfo.setLastUpdateTime(date);
                fileInfo.setDelFlag(NORMAL.getFlag());
                fileInfoMapper.insertSelective(fileInfo);
                //刷新内存信息
                try {
                    refreshUseSpace(userId);
                } catch (Exception e) {
                    logger.error("刷新内存信息失败",e);
                    throw new MyException("刷新内存信息失败",404);
                }
                //返回秒传信息
                UploadVO uploadVO = new UploadVO();
                uploadVO.setFileId(fileId);
                uploadVO.setStatus(UPLOAD_SECONDS.getStatus());
                return uploadVO;
            }
    }

    //没有现存文件，继续执行分片上传
    private UploadVO uploadBySplit(UploadDTO uploadDTO, String userId) throws Exception {
        if(uploadDTO.getChunks()>MAX_CHUNK_SIZE)
            throw new MyException("文件过大",ERROR_RES_CODE);
        String fileId = uploadDTO.getFileId();
        String tempSizeKey = RedisUtils.getTempSizeKey(userId, fileId);
        //获取用户空间信息和文件临时大小信息，校验用户空间余容是否足够
        RedisUseSpaceDTO useSpaceDTO = redisManager.get(RedisUtils.getUserSpaceKey(userId), RedisUseSpaceDTO.class);
        Long tempSize = Long.parseLong(redisManager.get(tempSizeKey, String.class));
        if(tempSize+uploadDTO.getFile().getSize()+useSpaceDTO.getUseSpace()>useSpaceDTO.getTotalSpace())
        {  //空间不足，删除临时大小信息，抛出异常
            redisManager.delete(tempSizeKey);
            // 删除临时切片文件
            minioUtils.deleteFolder(FILE_TEMP_PATH+userId+"/"+fileId);
            throw new MyException("内存空间不足",NO_SPACE_RES_CODE);
        }
        else
        {
            //空间足够，更新文件临时大小信息
            redisManager.setex(tempSizeKey, String.valueOf(tempSize+uploadDTO.getFile().getSize()), REDIS_TEMP_EXPIRE_TIME);
        }
        String tempFilePath = FILE_TEMP_PATH+ userId +"/"+ fileId +"/"+ uploadDTO.getChunkIndex();
        try {
            minioUtils.saveMultipartFile(tempFilePath, uploadDTO.getFile());
        } catch (MyException e) {
            logger.error(e.msg);
            throw new MyException("文件上传失败",FAIL_RES_CODE);
        }
        String status = null;
        if(uploadDTO.getChunks()-1 == uploadDTO.getChunkIndex())
        {
            status = UPLOAD_FINISH.getStatus();
            unionFile(uploadDTO, userId);//分片文件上传完毕，执行合并
        }
        else
            status = UPLOADING.getStatus();
        UploadVO uploadVO = new UploadVO();
        uploadVO.setFileId(fileId);
        uploadVO.setStatus(status);
        return uploadVO;
    }


    //整理文件信息并合并文件
    @Transactional
    public void unionFile(UploadDTO uploadDTO, String userId) throws Exception {
        try {
            //根据后缀获取文件分类信息
            String suffix = uploadDTO.getFileName().substring(uploadDTO.getFileName().lastIndexOf("."));
            FileTypeEnum typeEnum = FileTypeEnum.getTypeBySuffix(suffix);
            //获取日期信息
            Date date = new Date();
            String dateStr = StringUtils.formatDate(date,YEAR_MONTH_DAY.getPattern());
            //获取文件大小信息
            String fileId = uploadDTO.getFileId();
            String fileSizeStr = redisManager.get(RedisUtils.getTempSizeKey(userId, fileId), String.class);
            Long fileSize = Long.parseLong(fileSizeStr);
            String fileName = uploadDTO.getFileName();
            String filePath = FILE_ROOT_PATH+dateStr+"/"+fileId+"/"+fileName;
            String coverPath = filePath.substring(0,filePath.lastIndexOf("/"))+"/"+FILE_DEFAULT_COVER_NAME;
            int fileType = typeEnum.getType();
            int fileCategory = typeEnum.getCategory().getCategory();
            //装填文件信息
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
            fileInfo.setFolderType(FOLDER_TYPE_FILE);//默认是文件而非文件夹
            fileInfo.setFileType(fileType);
            fileInfo.setStatus(TRANSFERRING.getStatus());//默认正在转码
            fileInfo.setFileCategory(fileCategory);
            fileInfo.setDelFlag(NORMAL.getFlag());//默认正常
            fileInfoMapper.insertSelective(fileInfo);
            //刷新内存信息
            refreshUseSpace(userId);
            //指定RabbitMQ交换机发送对应路由键的mergeFile任务，发送前将mergeFile()所需参数的包装类封装成字符串发送
            String folderPath = filePath.substring(0,filePath.lastIndexOf("/"));
            MergeMessageDTO mergeMessageDTO = new MergeMessageDTO(uploadDTO, folderPath, userId);
            String mergeString = JSON.toJSONString(mergeMessageDTO, SerializerFeature.IgnoreErrorGetter);
            rabbitTemplate.convertAndSend(mergeMessageDTO.EXCHANGE_NAME,mergeMessageDTO.ROUTING_KEY,mergeString);
        } catch (Exception e) {
            logger.error("文件信息保存失败",e);
            throw new MyException("文件上传失败",FAIL_RES_CODE);
        }finally {
            //无论如何，删除临时文件大小信息
            redisManager.delete(RedisUtils.getTempSizeKey(userId, uploadDTO.getFileId()));
        }
    }
    //合并文件(该方法中凡是MinIO路径都添加了minio前缀，其余路径均表示本地路径)
    public void mergeFile(UploadDTO uploadDTO, String minioFolderPath, String userId) throws Exception {
        //设置文件信息更新体，用于更新转码状态
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(uploadDTO.getFileId());
        fileInfo.setUserId(userId);
        //创建本地合并文件地址，并创建对应File实例
        String localFolderPath = appProperties.getLocalPath()+uploadDTO.getFileId();
        String localMergeFilePath = localFolderPath+"/"+uploadDTO.getFileName();
        File mergeFile = new File(localMergeFilePath);
        if(!mergeFile.getParentFile().exists())
            mergeFile.getParentFile().mkdirs();
        FileOutputStream mergeOutputStream = new FileOutputStream(mergeFile);//创建输出流
        String minioTempPath = FILE_TEMP_PATH+userId+"/"+uploadDTO.getFileId();//分片文件目录
        try {
            //获取分片文件，将其合并写入到mergeFile
            for(int i = 0;i < MAX_CHUNK_SIZE;i++)
            {
                GetObjectResponse fileResponse = minioUtils.getFileResponse(minioTempPath + "/" + i);
                if(fileResponse == null)
                    break;
                fileResponse.transferTo(mergeOutputStream);
            }
            //关闭输出流
            mergeOutputStream.flush();
            mergeOutputStream.close();

            //合并成功，如果文件类型是图片或者视频，将生缩略图并对视频文件进行m3u8切片处理(以下操作均在服务器本地进行)
            String suffix = uploadDTO.getFileName().substring(uploadDTO.getFileName().lastIndexOf("."));
            if(FileTypeEnum.getTypeBySuffix(suffix) == FileTypeEnum.VIDEO)
            {
                //1 生成缩略图
                String coverPath = localFolderPath+"/"+FILE_DEFAULT_COVER_NAME;
                FfmpegUtils.createVideoCover(localMergeFilePath,coverPath);
                //2 将原视频转码成TS格式
                String tsFilePath = localFolderPath+"/"+FILE_DEFAULT_TS_NAME;
                FfmpegUtils.turnVideo2Ts(localMergeFilePath,tsFilePath);
                //3 对完整TS文件进行ts切片获得ts文件和m3u8文件
                String m3u8FilePath = localFolderPath+"/" +FILE_DEFAULT_M3U8_NAME;
                FfmpegUtils.cutTsVedio(tsFilePath,m3u8FilePath,localFolderPath);
                //4 删除完整的ts文件
                FileUtils.deleteFile(tsFilePath);
                //5 将视频原文件，封面，m3u8,ts切片文件上传到MinIO
                File[] files = new File(localFolderPath).listFiles();
                for(File file:files)
                {
                    String minioPath = minioFolderPath +"/"+file.getName();
                    minioUtils.saveFile(minioPath,file);
                }
            }
            else if(FileTypeEnum.getTypeBySuffix(suffix) == FileTypeEnum.IMAGE)
            {
                //1 生成缩略图
                String coverPath = localFolderPath+"/"+FILE_DEFAULT_COVER_NAME;
                FfmpegUtils.createImageCover(localMergeFilePath,coverPath);
                //2 保存文件到MinIO
                File[] files = new File(localFolderPath).listFiles();
                for(File file:files)
                {
                    String minioPath = minioFolderPath +"/"+file.getName();
                    minioUtils.saveFile(minioPath,file);
                }
            }
            else {
                minioUtils.saveFile(minioFolderPath+"/"+uploadDTO.getFileName(),mergeFile);
            }
            fileInfo.setStatus(TRANS_SUCCEED.getStatus());
            fileInfoMapper.updateByPrimaryKeySelective(fileInfo);
        }
        catch (Exception e)
        {
            logger.error("文件转码失败",e);
            //转码失败，更新数据库状态
            fileInfo.setStatus(TRANS_FAILED.getStatus());
            fileInfoMapper.updateByPrimaryKeySelective(fileInfo);
            throw new MyException("文件转码失败",FAIL_RES_CODE);
        }finally {
            //删除服务器本地文件和MinIO临时分片文件
            FileUtils.deleteFolder(localFolderPath);
            minioUtils.deleteFolder(minioTempPath);
        }
    }



    /**
     * 获取图片
     * @param response
     * @param imagePath
     * @throws Exception
     */
    @Override
    public void getImage(HttpServletResponse response, String imagePath) throws Exception {
        try {
            minioUtils.getFile(imagePath,response);
        }catch (MyException e)
        {
            logger.error(e.msg);
            throw new MyException("获取图片失败",FAIL_RES_CODE);
        }

    }

    /**
     * 获取视频文件的信息（即M3U8文件)
     *
     * @param response
     * @param msg (msg可能是文件id，也可能是ts文件名称)
     * @param userId
     * @param session (获取session,针对msg两种情况需要向session中暂存信息)
     * @throws MyException
     */

    @Override
    public void getVideoInfo(HttpServletResponse response, String msg, String userId, HttpSession session) throws Exception {
        // 1.如果请求信息以".ts"结尾说明已经获得m3u8文件，msg是ts文件名称，发送对应的ts文件
        // 2.否则收到的是文件id，需要根据id获取m3u8文件
            if(msg.endsWith(".ts"))
            {
                String videoFolder = (String)session.getAttribute(SESSION_VIDEO_PATH_KEY);
                String tsPath = videoFolder+"/"+msg;
                minioUtils.getFile(tsPath,response);
            }
            else {
            // 先查询文件信息，不限制userId，支持家庭文件访问
            LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
            fileInfoLqw.eq(FileInfo::getFileId,msg);
            fileInfoLqw.eq(FileInfo::getFileType,FileTypeEnum.VIDEO.getType());//查询视频类型的
            fileInfoLqw.eq(FileInfo::getDelFlag,NORMAL.getFlag());//查询文件正常的
            fileInfoLqw.eq(FileInfo::getStatus,TRANS_SUCCEED.getStatus());//查询转码成功的
            FileInfo fileInfo = fileInfoMapper.selectOne(fileInfoLqw);
            if(fileInfo == null)
                throw new MyException("文件不存在或已删除",FAIL_RES_CODE);
            // 校验文件访问权限
            validateFileAccess(fileInfo, userId);
            String filePath = fileInfo.getFilePath();
            String fileFolder = filePath.substring(0,filePath.lastIndexOf("/"));
            session.setAttribute(SESSION_VIDEO_PATH_KEY,fileFolder);//存储该文件路径，方便后续获取ts文件
            String m3u8Path = fileFolder+"/"+FILE_DEFAULT_M3U8_NAME;
            minioUtils.getFile(m3u8Path,response);
            }
    }

    /**
     * 下载获取文件
     * @param response
     * @param fileId
     * @param userId
     * @param session
     */
    @Override
    public void getFile(HttpServletResponse response, String fileId, String userId, HttpSession session) throws MyException {
        // 先查询文件信息，不限制userId，支持家庭文件访问
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getFileId,fileId);
        fileInfoLqw.eq(FileInfo::getDelFlag,NORMAL.getFlag());
        fileInfoLqw.eq(FileInfo::getStatus,TRANS_SUCCEED.getStatus());
        FileInfo fileInfo = fileInfoMapper.selectOne(fileInfoLqw);
        if(fileInfo == null)
            throw new MyException("文件不存在或已删除",FAIL_RES_CODE);
        // 校验文件访问权限
        validateFileAccess(fileInfo, userId);
        String filePath = fileInfo.getFilePath();
        try {
            minioUtils.getFile(filePath,response);
        }catch (Exception e)
            {
                logger.error(e.getMessage());
                throw new MyException("获取文件失败",FAIL_RES_CODE);
            }
    }

    /**
     * 校验文件访问权限
     * @param fileInfo 文件信息
     * @param userId 当前用户ID
     * @throws MyException 无权限访问时抛出异常
     */
    private void validateFileAccess(FileInfo fileInfo, String userId) throws MyException {
        // 如果是家庭文件，校验用户是否属于该家庭
        if (fileInfo.getBelongingHome() != null && !fileInfo.getBelongingHome().isEmpty()) {
            LambdaQueryWrapper<com.example.homic.model.FamilyMember> memberLqw = new LambdaQueryWrapper<>();
            memberLqw.eq(com.example.homic.model.FamilyMember::getFamilyId, fileInfo.getBelongingHome());
            memberLqw.eq(com.example.homic.model.FamilyMember::getUserId, userId);
            Long count = familyMemberMapper.selectCount(memberLqw);
            if (count == 0) {
                throw new MyException("无权限访问该文件", FAIL_RES_CODE);
            }
        } else {
            // 个人文件，校验userId是否匹配
            if (!fileInfo.getUserId().equals(userId)) {
                throw new MyException("无权限访问该文件", FAIL_RES_CODE);
            }
        }
    }

    /**
     * 创建下载链接
     * @param fileId
     * @param userId
     * @return
     */
    @Override
    public String getDownloadUrl(String fileId, String userId) throws MyException {
        // 先查询文件信息，不限制userId，支持家庭文件访问
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getFileId,fileId);
        fileInfoLqw.eq(FileInfo::getStatus,TRANS_SUCCEED.getStatus());//查询转码成功的
        fileInfoLqw.eq(FileInfo::getFolderType,FOLDER_TYPE_FILE);//查询是实体文件的
        FileInfo fileInfo = fileInfoMapper.selectOne(fileInfoLqw);
        if( fileInfo == null)
            throw new MyException("文件不存在或已删除",FAIL_RES_CODE);
        // 校验文件访问权限
        validateFileAccess(fileInfo, userId);
        //将路径存入redis,key是临时生成的code
        String filePath = fileInfo.getFilePath();
        String downloadUrlCode = StringUtils.getSerialNumber(30);
        redisManager.setex(RedisUtils.getDownloadCodeKey(downloadUrlCode), filePath, REDIS_DOWNLOAD_EXPIRE_TIME);
        return downloadUrlCode;
    }

    /**
     * 通过code下载文件
     * @param code
     * @param response
     */
    @Override
    public void downloadFile(String code, HttpServletResponse response) throws MyException {
        String filePath = redisManager.get(RedisUtils.getDownloadCodeKey(code), String.class);
        try {
            minioUtils.getFile(filePath,response);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new MyException("下载文件失败",FAIL_RES_CODE);
        }
    }
}
