package com.example.homic.services.implement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.homic.config.RedisManager;
import com.example.homic.constants.NormalConstants;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.FriendRelationMapper;
import com.example.homic.mapper.FriendRequestMapper;
import com.example.homic.mapper.UserInfoMapper;
import com.example.homic.model.FriendFileShare;
import com.example.homic.model.FriendRelation;
import com.example.homic.model.FriendRequest;
import com.example.homic.model.UserInfo;
import com.example.homic.services.FriendService;
import com.example.homic.utils.RedisUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.FriendCodeVO;
import com.example.homic.vo.FriendInfoVO;
import com.example.homic.vo.FriendRequestVO;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UserSimpleInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.example.homic.constants.CodeConstants.*;

/**
 * 好友服务实现类
 * 作者：Rocky23318
 * 时间：2026
 * 项目名：homic
 */
@Service
public class FriendServiceImpl implements FriendService {

    private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisManager redisManager;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private FriendRelationMapper friendRelationMapper;

    // 好友码长度
    private static final int FRIEND_CODE_LENGTH = 16;

    // 好友码过期时间（秒）：60秒
    private static final long FRIEND_CODE_EXPIRE_TIME = 60;

    // 生成好友码的冷却时间（秒）：15秒
    private static final long GENERATE_COOLDOWN_TIME = 15;

    /**
     * 生成好友码
     * 确保生成的好友码唯一，通过Redis检查是否已存在
     * 限制：15秒内只能生成一次
     *
     * @return 包含好友码和过期时间的响应，或冷却时间
     * @throws MyException
     */
    @Override
    public ResponseVO generateFriendCode() throws MyException {
        try {
            // 从当前请求中获取用户ID
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 验证用户是否存在
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
            if (userInfo == null) {
                throw new MyException("用户不存在", FAIL_RES_CODE);
            }

            // 检查冷却时间
            String cooldownKey = RedisUtils.buildKey("friend:code:cooldown", userId);
            if (redisManager.hasKey(cooldownKey)) {
                Long remainingCooldown = redisManager.getExpire(cooldownKey);
                logger.warn("用户 {} 在冷却期内尝试生成好友码，剩余冷却时间: {}秒", userId, remainingCooldown);
                throw new MyException("生成好友码过于频繁，请" + remainingCooldown + "秒后再试", FAIL_RES_CODE);
            }

            // 生成唯一的好友码
            String friendCode = generateUniqueFriendCode();

            // 将好友码存储到Redis，关联到用户ID
            String redisKey = RedisUtils.buildKey("friend:code", friendCode);
            redisManager.setex(redisKey, userId, FRIEND_CODE_EXPIRE_TIME);

            // 同时存储用户ID到好友码的映射，用于查询自身好友码
            String userCodeKey = RedisUtils.buildKey("user:friend:code", userId);
            redisManager.setex(userCodeKey, friendCode, FRIEND_CODE_EXPIRE_TIME);

            // 设置冷却时间
            redisManager.setex(cooldownKey, "1", GENERATE_COOLDOWN_TIME);

            logger.info("用户 {} 成功生成好友码: {}", userId, friendCode);

            // 返回好友码和过期时间
            FriendCodeVO friendCodeVO = new FriendCodeVO(friendCode, FRIEND_CODE_EXPIRE_TIME, GENERATE_COOLDOWN_TIME);
            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("好友码生成成功");
            responseVO.setData(friendCodeVO);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("生成好友码失败", e);
            throw new MyException("生成好友码失败", FAIL_RES_CODE);
        }
    }

    /**
     * 通过好友码查询用户信息
     *
     * @param friendCode 好友码
     * @return 用户简化信息（仅包含id、昵称、头像）
     * @throws MyException
     */
    @Override
    public UserSimpleInfoVO getUserByFriendCode(String friendCode) throws MyException {
        try {
            // 从Redis中获取好友码对应的用户ID
            String redisKey = RedisUtils.buildKey("friend:code", friendCode);
            String userId = redisManager.get(redisKey, String.class);

            if (userId == null) {
                throw new MyException("好友码不存在或已过期", FAIL_RES_CODE);
            }

            // 根据用户ID查询用户信息
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
            if (userInfo == null) {
                throw new MyException("用户不存在", FAIL_RES_CODE);
            }

            // 返回简化的用户信息
            return new UserSimpleInfoVO(userInfo.getUserId(), userInfo.getNickName(), userInfo.getUserAvatar());
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("通过好友码查询用户失败", e);
            throw new MyException("查询用户失败", FAIL_RES_CODE);
        }
    }

    /**
     * 查询自身好友码
     * 如果用户没有有效的好友码，返回未生成状态
     *
     * @return 包含好友码、过期时间和冷却时间的响应
     * @throws MyException
     */
    @Override
    public ResponseVO getMyFriendCode() throws MyException {
        try {
            // 从当前请求中获取用户ID
            String userId = getCurrentUserId();
            if (userId == null) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 验证用户是否存在
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
            if (userInfo == null) {
                throw new MyException("用户不存在", FAIL_RES_CODE);
            }

            // 查询用户是否已有有效的好友码
            String existingCode = findFriendCodeByUserId(userId);
            if (existingCode != null) {
                // 获取好友码的剩余过期时间
                String redisKey = RedisUtils.buildKey("friend:code", existingCode);
                Long expiryTime = redisManager.getExpire(redisKey);

                // 获取重新生成的冷却时间
                String cooldownKey = RedisUtils.buildKey("friend:code:cooldown", userId);
                Long regenerateCooldown = 0L;
                if (redisManager.hasKey(cooldownKey)) {
                    regenerateCooldown = redisManager.getExpire(cooldownKey);
                }

                FriendCodeVO friendCodeVO = new FriendCodeVO(existingCode, expiryTime, regenerateCooldown);
                ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
                responseVO.setCode(SUCCESS_RES_CODE);
                responseVO.setInfo("好友码查询成功");
                responseVO.setData(friendCodeVO);
                return responseVO;
            }

            // 如果没有有效的好友码，返回未生成状态
            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("好友码未生成");
            responseVO.setData(null);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("查询自身好友码失败", e);
            throw new MyException("查询好友码失败", FAIL_RES_CODE);
        }
    }

    /**
     * 根据用户ID查找其有效的好友码
     *
     * @param userId 用户ID
     * @return 好友码，如果不存在则返回null
     */
    private String findFriendCodeByUserId(String userId) {
        try {
            String userCodeKey = RedisUtils.buildKey("user:friend:code", userId);
            return redisManager.get(userCodeKey, String.class);
        } catch (Exception e) {
            logger.error("查询用户好友码映射失败", e);
            return null;
        }
    }

    /**
     * 从当前HTTP请求中获取用户ID
     *
     * @return 当前用户ID
     */
    private String getCurrentUserId() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
                SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(NormalConstants.SESSION_USER_INFO_KEY);
                if (userInfo != null) {
                    return userInfo.getUserId();
                }
            }
        } catch (Exception e) {
            logger.error("获取当前用户ID失败", e);
        }
        return null;
    }

    /**
     * 生成唯一的好友码
     * 通过循环生成和检查，确保好友码在Redis中不存在
     * 生成16位大写字母和数字的组合
     *
     * @return 唯一的好友码
     */
    private String generateUniqueFriendCode() {
        String friendCode;
        int maxRetries = 10;
        int retries = 0;

        do {
            // 生成随机的好友码（大写字母和数字组合）
            friendCode = generateUppercaseAlphanumeric(FRIEND_CODE_LENGTH);
            retries++;

            // 如果好友码已存在，继续生成
            if (retries >= maxRetries) {
                logger.warn("生成唯一好友码达到最大重试次数");
                break;
            }
        } while (redisManager.hasKey(RedisUtils.buildKey("friend:code", friendCode)));

        return friendCode;
    }

    /**
     * 生成指定长度的大写字母和数字组合
     *
     * @param length 长度
     * @return 大写字母和数字组合的字符串
     */
    private String generateUppercaseAlphanumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }

    /**
     * 发送好友申请
     *
     * @param userId 当前用户ID
     * @param friendId 被申请者的用户ID
     * @return 响应
     * @throws MyException
     */
    @Override
    public ResponseVO sendFriendRequest(String userId, String friendId) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 获取当前用户信息，检查是否为关怀账号
            UserInfo currentUser = userInfoMapper.selectByPrimaryKey(userId);
            if (currentUser == null) {
                throw new MyException("用户不存在", FAIL_RES_CODE);
            }
            if (currentUser.getIsDummy() != null && currentUser.getIsDummy() == 1) {
                throw new MyException("关怀账号不支持添加好友", FAIL_RES_CODE);
            }

            // 验证被申请者是否存在
            UserInfo targetUser = userInfoMapper.selectByPrimaryKey(friendId);
            if (targetUser == null) {
                throw new MyException("用户不存在", FAIL_RES_CODE);
            }

            // 检查目标用户是否为关怀账号
            if (targetUser.getIsDummy() != null && targetUser.getIsDummy() == 1) {
                throw new MyException("关怀账号不支持添加好友", FAIL_RES_CODE);
            }

            // 不能给自己发送好友申请
            if (userId.equals(friendId)) {
                throw new MyException("不能给自己发送好友申请", FAIL_RES_CODE);
            }

            // 检查是否已经是好友
            FriendRelation existingRelation = friendRelationMapper.selectRelation(userId, friendId);
            if (existingRelation != null && existingRelation.getStatus() == 1) {
                throw new MyException("你们已经是好友了", FAIL_RES_CODE);
            }

            // 检查是否已有申请记录
            FriendRequest existingRequest = friendRequestMapper.selectRequest(userId, friendId);
            if (existingRequest != null && existingRequest.getStatus() == 0) {
                // 待处理状态，不允许重复申请
                throw new MyException("已经发送过申请，请等待对方处理", FAIL_RES_CODE);
            }
            // 如果状态是已拒绝(2)或已接受(1)，允许创建新的申请记录

            // 创建新的好友申请
            FriendRequest friendRequest = new FriendRequest();
            // requestId由MyBatis-Plus自动生成（使用雪花算法）
            friendRequest.setUserId(userId);
            friendRequest.setFriendId(friendId);
            friendRequest.setStatus(0); // 0-已申请
            friendRequest.setMessage("");
            friendRequest.setCreateTime(new Date());
            friendRequest.setUpdateTime(new Date());

            friendRequestMapper.insert(friendRequest);

            logger.info("用户 {} 向用户 {} 发送了好友申请", userId, friendId);

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("好友申请已发送");
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("发送好友申请失败", e);
            throw new MyException("发送好友申请失败", FAIL_RES_CODE);
        }
    }

    /**
     * 查询好友申请状态
     *
     * @param userId 当前用户ID
     * @param friendId 被申请者的用户ID
     * @return 申请状态 (0-未申请, 1-已申请, 2-已接受, 3-已拒绝)
     * @throws MyException
     */
    @Override
    public Integer getFriendRequestStatus(String userId, String friendId) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 检查是否已经是好友
            FriendRelation existingRelation = friendRelationMapper.selectRelation(userId, friendId);
            if (existingRelation != null && existingRelation.getStatus() == 1) {
                return 2; // 已接受（已是好友）
            }

            // 查询申请记录
            FriendRequest friendRequest = friendRequestMapper.selectRequest(userId, friendId);
            if (friendRequest == null) {
                return 0; // 未申请
            }

            // 根据申请状态返回
            if (friendRequest.getStatus() == 0) {
                return 1; // 已申请（待处理）
            } else if (friendRequest.getStatus() == 1) {
                return 2; // 已接受
            } else if (friendRequest.getStatus() == 2) {
                return 3; // 已拒绝
            }

            return 0; // 默认未申请
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("查询好友申请状态失败", e);
            throw new MyException("查询申请状态失败", FAIL_RES_CODE);
        }
    }

    /**
     * 获取我发出的好友申请列表
     *
     * @param userId 当前用户ID
     * @return 好友申请列表
     * @throws MyException
     */
    @Override
    public ResponseVO getSentRequests(String userId) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 查询我发出的所有申请
            List<FriendRequest> requests = friendRequestMapper.selectAllSentRequests(userId);

            // 转换为VO对象，包含对方的个人信息
            List<FriendRequestVO> requestVOList = new java.util.ArrayList<>();
            for (FriendRequest request : requests) {
                FriendRequestVO vo = new FriendRequestVO();
                vo.setId(request.getRequestId());
                vo.setSenderId(request.getUserId());
                vo.setReceiverId(request.getFriendId());
                vo.setStatus(request.getStatus());
                vo.setMessage(request.getMessage());
                vo.setCreateTime(request.getCreateTime());
                vo.setUpdateTime(request.getUpdateTime());

                // 查询发送者信息（当前用户）
                UserInfo senderInfo = userInfoMapper.selectByPrimaryKey(request.getUserId());
                if (senderInfo != null) {
                    vo.setSenderNickName(senderInfo.getNickName());
                    vo.setSenderAvatar(senderInfo.getUserAvatar());
                }

                // 查询接收者信息
                UserInfo receiverInfo = userInfoMapper.selectByPrimaryKey(request.getFriendId());
                if (receiverInfo != null) {
                    vo.setReceiverNickName(receiverInfo.getNickName());
                    vo.setReceiverAvatar(receiverInfo.getUserAvatar());
                }

                requestVOList.add(vo);
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("查询成功");
            responseVO.setData(requestVOList);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取发出的好友申请列表失败", e);
            throw new MyException("获取申请列表失败", FAIL_RES_CODE);
        }
    }

    /**
     * 获取我收到的好友申请列表
     *
     * @param userId 当前用户ID
     * @return 好友申请列表
     * @throws MyException
     */
    @Override
    public ResponseVO getReceivedRequests(String userId) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 查询我收到的所有申请
            List<FriendRequest> requests = friendRequestMapper.selectAllReceivedRequests(userId);

            // 转换为VO对象，包含对方的个人信息
            List<FriendRequestVO> requestVOList = new java.util.ArrayList<>();
            for (FriendRequest request : requests) {
                FriendRequestVO vo = new FriendRequestVO();
                vo.setId(request.getRequestId());
                vo.setSenderId(request.getUserId());
                vo.setReceiverId(request.getFriendId());
                vo.setStatus(request.getStatus());
                vo.setMessage(request.getMessage());
                vo.setCreateTime(request.getCreateTime());
                vo.setUpdateTime(request.getUpdateTime());

                // 查询发送者信息
                UserInfo senderInfo = userInfoMapper.selectByPrimaryKey(request.getUserId());
                if (senderInfo != null) {
                    vo.setSenderNickName(senderInfo.getNickName());
                    vo.setSenderAvatar(senderInfo.getUserAvatar());
                }

                // 查询接收者信息（当前用户）
                UserInfo receiverInfo = userInfoMapper.selectByPrimaryKey(request.getFriendId());
                if (receiverInfo != null) {
                    vo.setReceiverNickName(receiverInfo.getNickName());
                    vo.setReceiverAvatar(receiverInfo.getUserAvatar());
                }

                requestVOList.add(vo);
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("查询成功");
            responseVO.setData(requestVOList);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取收到的好友申请列表失败", e);
            throw new MyException("获取申请列表失败", FAIL_RES_CODE);
        }
    }

    /**
     * 处理好友申请
     *
     * @param userId 当前用户ID
     * @param requestId 申请ID
     * @param action 操作 (1-接受, 2-拒绝)
     * @return 响应
     * @throws MyException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO handleFriendRequest(String userId, Long requestId, Integer action) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 验证操作类型
            if (action != 1 && action != 2) {
                throw new MyException("无效的操作类型", FAIL_RES_CODE);
            }

            // 查询申请记录
            FriendRequest friendRequest = friendRequestMapper.selectById(requestId);
            if (friendRequest == null) {
                throw new MyException("申请记录不存在", FAIL_RES_CODE);
            }

            // 验证当前用户是否为接收者
            if (!userId.equals(friendRequest.getFriendId())) {
                throw new MyException("无权处理此申请", FAIL_RES_CODE);
            }

            // 验证申请状态
            if (friendRequest.getStatus() != 0) {
                throw new MyException("申请已被处理", FAIL_RES_CODE);
            }

            // 更新申请状态
            friendRequest.setStatus(action);
            friendRequest.setUpdateTime(new Date());
            friendRequestMapper.updateById(friendRequest);

            // 如果接受申请，创建好友关系
            if (action == 1) {
                // 创建双向好友关系
                FriendRelation relation1 = new FriendRelation();
                // relationId由MyBatis-Plus自动生成（使用雪花算法）
                relation1.setUserId(friendRequest.getUserId());
                relation1.setFriendId(friendRequest.getFriendId());
                relation1.setStatus(1); // 1-正常
                relation1.setCreateTime(new Date());
                relation1.setUpdateTime(new Date());
                friendRelationMapper.insert(relation1);

                FriendRelation relation2 = new FriendRelation();
                // relationId由MyBatis-Plus自动生成（使用雪花算法）
                relation2.setUserId(friendRequest.getFriendId());
                relation2.setFriendId(friendRequest.getUserId());
                relation2.setStatus(1); // 1-正常
                relation2.setCreateTime(new Date());
                relation2.setUpdateTime(new Date());
                friendRelationMapper.insert(relation2);

                logger.info("用户 {} 接受了用户 {} 的好友申请", userId, friendRequest.getUserId());
            } else {
                logger.info("用户 {} 拒绝了用户 {} 的好友申请", userId, friendRequest.getUserId());
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo(action == 1 ? "已接受好友申请" : "已拒绝好友申请");
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("处理好友申请失败", e);
            throw new MyException("处理申请失败", FAIL_RES_CODE);
        }
    }

    /**
     * 获取我的好友列表
     *
     * @param userId 当前用户ID
     * @return 好友列表
     * @throws MyException
     */
    @Override
    public ResponseVO getMyFriends(String userId) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 查询所有好友关系
            List<FriendRelation> relations = friendRelationMapper.selectFriendsByUserId(userId);

            // 转换为VO对象，包含好友的个人信息
            List<FriendInfoVO> friendList = new java.util.ArrayList<>();
            for (FriendRelation relation : relations) {
                FriendInfoVO vo = new FriendInfoVO();
                vo.setRelationId(relation.getRelationId());
                vo.setFriendId(relation.getFriendId());
                vo.setRemark(relation.getRemark());
                vo.setIsSpecial(relation.getIsSpecial() != null ? relation.getIsSpecial() : 0);
                vo.setCreateTime(relation.getCreateTime());

                // 查询好友信息
                UserInfo friendInfo = userInfoMapper.selectByPrimaryKey(relation.getFriendId());
                if (friendInfo != null) {
                    vo.setNickName(friendInfo.getNickName());
                    vo.setAvatar(friendInfo.getUserAvatar());
                }

                friendList.add(vo);
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("查询成功");
            responseVO.setData(friendList);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取好友列表失败", e);
            throw new MyException("获取好友列表失败", FAIL_RES_CODE);
        }
    }

    /**
     * 获取特别关注的好友列表
     *
     * @param userId 当前用户ID
     * @return 好友列表
     * @throws MyException
     */
    @Override
    public ResponseVO getSpecialFriends(String userId) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 查询特别关注的好友
            List<FriendRelation> relations = friendRelationMapper.selectSpecialFriends(userId);

            // 转换为VO对象，包含好友的个人信息
            List<FriendInfoVO> friendList = new java.util.ArrayList<>();
            for (FriendRelation relation : relations) {
                FriendInfoVO vo = new FriendInfoVO();
                vo.setRelationId(relation.getRelationId());
                vo.setFriendId(relation.getFriendId());
                vo.setRemark(relation.getRemark());
                vo.setIsSpecial(relation.getIsSpecial() != null ? relation.getIsSpecial() : 0);
                vo.setCreateTime(relation.getCreateTime());

                // 查询好友信息
                UserInfo friendInfo = userInfoMapper.selectByPrimaryKey(relation.getFriendId());
                if (friendInfo != null) {
                    vo.setNickName(friendInfo.getNickName());
                    vo.setAvatar(friendInfo.getUserAvatar());
                }

                friendList.add(vo);
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("查询成功");
            responseVO.setData(friendList);
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取特别关注列表失败", e);
            throw new MyException("获取特别关注列表失败", FAIL_RES_CODE);
        }
    }

    /**
     * 更新好友备注
     *
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @param remark 备注名
     * @return 响应
     * @throws MyException
     */
    @Override
    public ResponseVO updateFriendRemark(String userId, String friendId, String remark) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 查询好友关系
            FriendRelation relation = friendRelationMapper.selectRelation(userId, friendId);
            if (relation == null || relation.getStatus() != 1) {
                throw new MyException("好友关系不存在", FAIL_RES_CODE);
            }

            // 更新备注
            relation.setRemark(remark);
            relation.setUpdateTime(new Date());
            friendRelationMapper.updateById(relation);

            logger.info("用户 {} 更新了好友 {} 的备注为: {}", userId, friendId, remark);

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("备注更新成功");
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("更新好友备注失败", e);
            throw new MyException("更新备注失败", FAIL_RES_CODE);
        }
    }

    /**
     * 切换好友特别关注状态
     *
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @param isSpecial 是否特别关注
     * @return 响应
     * @throws MyException
     */
    @Override
    public ResponseVO toggleSpecialAttention(String userId, String friendId, Boolean isSpecial) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 查询好友关系
            FriendRelation relation = friendRelationMapper.selectRelation(userId, friendId);
            if (relation == null || relation.getStatus() != 1) {
                throw new MyException("好友关系不存在", FAIL_RES_CODE);
            }

            // 更新特别关注状态
            relation.setIsSpecial(isSpecial ? 1 : 0);
            relation.setUpdateTime(new Date());
            friendRelationMapper.updateById(relation);

            logger.info("用户 {} {} 好友 {} 的特别关注", userId, isSpecial ? "添加了" : "取消了", friendId);

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo(isSpecial ? "已添加特别关注" : "已取消特别关注");
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("切换特别关注状态失败", e);
            throw new MyException("操作失败", FAIL_RES_CODE);
        }
    }

    /**
     * 删除好友
     *
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 响应
     * @throws MyException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO deleteFriend(String userId, String friendId) throws MyException {
        try {
            // 验证用户ID不为空
            if (userId == null || userId.isEmpty()) {
                throw new MyException("用户未登录", FAIL_RES_CODE);
            }

            // 查询双向好友关系
            FriendRelation relation1 = friendRelationMapper.selectRelation(userId, friendId);
            FriendRelation relation2 = friendRelationMapper.selectRelation(friendId, userId);

            if (relation1 == null || relation1.getStatus() != 1) {
                throw new MyException("好友关系不存在", FAIL_RES_CODE);
            }

            // 删除双向好友关系
            friendRelationMapper.deleteById(relation1.getRelationId());
            if (relation2 != null) {
                friendRelationMapper.deleteById(relation2.getRelationId());
            }

            logger.info("用户 {} 删除了好友 {}", userId, friendId);

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
            responseVO.setCode(SUCCESS_RES_CODE);
            responseVO.setInfo("好友已删除");
            return responseVO;
        } catch (MyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("删除好友失败", e);
            throw new MyException("删除好友失败", FAIL_RES_CODE);
        }
    }
}
