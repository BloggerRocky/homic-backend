package com.example.homic.services.implement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.homic.dto.session.SessionShareInfoDTO;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.FileInfoMapper;
import com.example.homic.mapper.FileShareMapper;
import com.example.homic.mapper.UserInfoMapper;
import com.example.homic.model.file.FileInfo;
import com.example.homic.model.file.FileShare;
import com.example.homic.model.UserInfo;
import com.example.homic.services.ShareService;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.ShareInfoVO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

import static com.example.homic.constants.CodeConstants.FAIL_RES_CODE;
import static com.example.homic.constants.NormalConstants.*;
import static com.example.homic.constants.enums.FileFlagEnum.NORMAL;
import static com.example.homic.constants.enums.FileStatusEnum.TRANS_SUCCEED;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.12:24
 * 项目名：homic
 */
@Service
public class ShareServiceImpl implements ShareService {
    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    FileShareMapper fileShareMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    ModelMapper modelMapper;

    Logger logger = LoggerFactory.getLogger(ShareServiceImpl.class);

    /**
     * 加载分享列表
     * @param pageNoStr
     * @param pageSizeStr
     * @param userId
     * @return
     * @throws MyException
     */
    @Override
    public PageResultVO loadShareList(String pageNoStr, String pageSizeStr, String userId) throws MyException {
        try {
            Long pageNo = pageNoStr.equals("") ? 1L : Long.parseLong(pageNoStr);
            Long pageSize = pageSizeStr.equals("") ? DEFAULT_PAGE_SIZE : Long.parseLong(pageSizeStr);
            IPage<FileShare> page = new Page<>(pageNo,pageSize);
            LambdaQueryWrapper<FileShare> fileShareLqw = new LambdaQueryWrapper<>();
            fileShareMapper.selectPage(page,fileShareLqw.eq(FileShare::getUserId,userId));
            PageResultVO<FileShare> fileSharePageResultVO = new PageResultVO<>(page, FileShare.class);
            return fileSharePageResultVO;
        } catch (Exception e) {
            throw new MyException("查询失败",FAIL_RES_CODE);
        }
    }

    /**
     * 创建分享链接
     * @param fileId
     * @param validType
     * @param codeType
     * @param code
     * @param userId
     * @return
     * @throws MyException
     */
    @Override
    public FileShare shareFile(String fileId, String validType, String codeType, String code, String userId) throws MyException {
        LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
        fileInfoLqw.eq(FileInfo::getFileId,fileId);
        fileInfoLqw.eq(FileInfo::getUserId,userId);
        fileInfoLqw.eq(FileInfo::getDelFlag,NORMAL.getFlag());
        fileInfoLqw.eq(FileInfo::getStatus,TRANS_SUCCEED.getStatus());
        try {
            FileInfo fileInfo = fileInfoMapper.selectOne(fileInfoLqw);
            if(fileInfo == null)
                return null;
            else{
                //生成分享id
                String shareId = StringUtils.getSerialNumber(20);
                //生成或沿用提取码
                code = codeType.equals("1") ? StringUtils.getRandomNumber(SHARE_CODE_LENGTH) : code;
                //生成过期时间
                Integer expireDay = SHARE_EXPIRE_TIME_MAP.get(validType);
                //装填信息
                FileShare fileShare = new FileShare();
                fileShare.setFileId(fileId);
                fileShare.setShareId(shareId);
                fileShare.setUserId(userId);
                //必须创建两个时间变量，否则会覆盖
                Date date = new Date();
                Date expireTime = new Date();
                expireTime.setTime(expireTime.getTime() + expireDay * 24 * 60 * 60 * 1000);
                fileShare.setShareTime(date);
                fileShare.setExpireTime(expireTime);
                fileShare.setValidType(Integer.parseInt(validType));
                fileShare.setCode(code);
                fileShare.setFileName(fileInfo.getFileName());
                fileShare.setFileCategory(fileInfo.getFileCategory());
                fileShare.setFileType(fileInfo.getFileType());
                fileShare.setFileCover(fileInfo.getFileCover());
                fileShare.setFolderType(fileInfo.getFolderType());
                fileShare.setShowCount(0);
                fileShareMapper.insert(fileShare);
                return fileShare;
            }
        } catch (Exception e) {
            logger.error("文件分享失败",e);
            throw new MyException("文件分享失败",FAIL_RES_CODE);
        }
    }

    /**
     * 取消分享链接
     * @param shareIds
     * @param userId
     * @return
     */
    @Override
    public boolean cancelShare(String shareIds, String userId) throws MyException {
        String[] ids = shareIds.split(",");
        LambdaQueryWrapper<FileShare> fileShareLqw = new LambdaQueryWrapper<>();
        fileShareLqw.eq(FileShare::getUserId,userId);
        fileShareLqw.in(FileShare::getShareId,ids);
        try {
            fileShareMapper.delete(fileShareLqw);
        } catch (Exception e) {
            throw new MyException("取消分享失败",FAIL_RES_CODE);
        }
        return true;
    }

    /**
     * 获取分享链接信息
     * @param shareId
     * @param userId
     * @return
     */
    @Override
    public ShareInfoVO getShareInfo(String shareId, String userId ) {
        LambdaQueryWrapper<FileShare> fileShareLqw = new LambdaQueryWrapper<>();
        fileShareLqw.eq(FileShare::getShareId,shareId);
        FileShare fileShare = fileShareMapper.selectOne(fileShareLqw);
        if(fileShare == null || fileShare.getExpireTime().before(new Date()))
            return null;
        ShareInfoVO shareInfoVO = modelMapper.map(fileShare,ShareInfoVO.class);
        if(shareInfoVO.getUserId() == userId)
            shareInfoVO.setCurrentUser(true);
        else
            shareInfoVO.setCurrentUser(false);
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(shareInfoVO.getUserId());
        shareInfoVO.setNickName(userInfo.getNickName());
        shareInfoVO.setAvatar(userInfo.getUserAvatar());
        return shareInfoVO;
    }


    //校验分享提取码
    public ShareInfoVO checkShareCode(String shareId, String code, HttpSession session) {
        LambdaQueryWrapper<FileShare> fileShareLqw = new LambdaQueryWrapper<>();
        fileShareLqw.eq(FileShare::getShareId,shareId);
        fileShareLqw.eq(FileShare::getCode,code);
        FileShare shareInfo = fileShareMapper.selectOne(fileShareLqw);
        if(shareInfo == null)
            return null;
        else {
            SessionShareInfoDTO sessionShareInfoDTO = new SessionShareInfoDTO();
            sessionShareInfoDTO.setShareId(shareId);
            sessionShareInfoDTO.setShareUserId(shareInfo.getUserId());
            sessionShareInfoDTO.setExpireTime(shareInfo.getExpireTime());
            sessionShareInfoDTO.setFileId(shareInfo.getFileId());
            sessionShareInfoDTO.setShareTime(shareInfo.getShareTime());
            session.setAttribute(SESSION_SHARE_KEY_PREFIX+shareId,sessionShareInfoDTO);
        }
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        String userId = userInfo.getUserId();
        return getShareInfo(shareId,userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveShareFile(String[] shareFileIdArray, String userId, String myFolderId) {
        try {
            LambdaQueryWrapper<FileInfo> fileInfoLqw = new LambdaQueryWrapper<>();
            fileInfoLqw.in(FileInfo::getFileId,shareFileIdArray);
            List<FileInfo> fileInfoList = fileInfoMapper.selectList(fileInfoLqw);
            if(fileInfoList == null || fileInfoList.size() == 0)
                return false;
            for(FileInfo fileInfo : fileInfoList)
            {
                String fileId = StringUtils.getSerialNumber(15);
                fileInfo.setFileId(fileId);
                fileInfo.setFilePid(myFolderId);
                fileInfo.setUserId(userId);
                fileInfo.setCreateTime(new Date());
                fileInfoMapper.insert(fileInfo);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
