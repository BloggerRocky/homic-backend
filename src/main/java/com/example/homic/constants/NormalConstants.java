package com.example.homic.constants;

import com.example.homic.dto.redis.RedisSettingDTO;

import java.util.Map;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.12:40
 * 项目名：homic
 */
//用于存储固定信息的类
public class NormalConstants {

    /**
     * 文件路径索引
     */
    public static final String FILE_ROOT_PATH = "file/";
    public static final String FILE_SYSTEM_PATH = "file/0000-00-00/system/";
    public static final String FILE_AVATAR_PATH = "avatar/";
    public static final String FILE_TEMP_PATH  = "temp/";
    public static final String FILE_CUTTING_FOLDER = "cuttings";
    public static final String FILE_DEFAULT_TS_NAME  = "index.ts";
    public static final String FILE_DEFAULT_M3U8_NAME  = "index.m3u8";
    public static final String FILE_DEFAULT_ORIGIN_FILE_NAME  = "originFile";
    public static final String FILE_DEFAULT_COVER_NAME  = "cover.jpg";
    /**
     * 文件信息中文件类型（0为文件,1为文件夹目录)
     */
    public static final int FOLDER_TYPE_FOLDER = 1;//文件夹
    public static final int FOLDER_TYPE_FILE  = 0;//文件
    /**
     * 邮箱验证码文本信息
     */
    public static final String REGIS_TITLE = "易云注册账号邮箱验证码";
    public static final String REGIS_CONTEXT = "您好，您用于注册易云网盘的邮箱验证码是：%s，该验证码15分钟内有效。\n若非本人操作，请忽略此邮件。";
    public static final String RESETTING_TITLE = "易云重置密码邮箱验证码";
    public static final String RESETTING_CONTEXT = "您好，您用于重置易云网盘账号密码的邮箱验证码是：%s，该验证码15分钟内有效。\n若非本人操作，请忽略此邮件。";
    /**
     * Redis前缀或键
     */
    public static final String REDIS_REGIS_CODE_PREFIX = "regis:code:";
    public static final String REDIS_RESETTING_CODE_PREFIX = "resetting:code:";
    public static final String REDIS_SYSTEM_SETTING_KEY = "system:setting:default_config";
    public static final String REDIS_USER_SPACE_PREFIX = "user:space:";
    public static final String REDIS_TEMP_SIZE_PREFIX = "temp:space:";
    public static final String REDIS_DOWNLOAD_CODE_PREFIX = "download:code:";
    /**
     * redis过期时间
     */
    public static final Long REDIS_DEFAULT_EXPIRE_TIME = 60*60*24L;
    public static final Long REDIS_TEMP_EXPIRE_TIME = 60*5L;//临时空间信息过期时间
    public static final Long REDIS_DOWNLOAD_EXPIRE_TIME = 60*5L;
    /**
     * Sesson KEY
     */
    public static final String SESSION_USER_INFO_KEY = "user_info";
    public static final String SESSION_VIDEO_PATH_KEY = "video_path";
    public static final String SESSION_SHARE_KEY_PREFIX = "share_info_";
    /**
     * 用户状态
     */
    public static final Integer USER_STATUS_ENABLE = 1;
    public static final Integer USER_STATUS_DISABLE = 0;
    /**
     * 一些数据字段的默认大小
     */
    public static final int CHECK_CODE_LENGTH = 4;
    public static final int EMAIL_CODE_LENGTH = 5;
    public static final int SHARE_CODE_LENGTH = 5;
    public static final int USER_ID_LENGTH = 15;
    public static final int FILE_ID_LENGTH = 15;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_CHUNK_SIZE = 2048;//最大分片数量
    public static final Long  USER_DEFAULT_TOTAL_SPACE = 1024L;//默认总空间1024M
    public static final Long  USER_DEFAULT_USE_SPACE = 0L;
    /**
     * 其他默认信息
     */

    public static final String[] PARAM_TYPE_ARRAY = {"java.lang.String","java.lang.Integer","java.lang.Long"};//参数基础数据类引用
    public static final RedisSettingDTO DEFAULT_SETTING_INFO = new RedisSettingDTO(REGIS_TITLE,REGIS_CONTEXT,USER_DEFAULT_TOTAL_SPACE);
    public static final String SESSION_REFERENCE = "javax.servlet.http.HttpSession";//Session引用
    public static final String[] PICTURE_TYPE_ARRAY = {".img",".jpeg",".jpg",".png"};//图片格式数组

    public static final String DEFAULT_AVATAR_FILE_NAME = "default_avatar.jpeg";//默认头像文件名
    public static final String DEFAULT_FOLDER_ICON_NAME ="default_folder_icon.jpg";//默认文件夹图片地址
    public static final Map<String,Integer> SHARE_EXPIRE_TIME_MAP = Map.of("0",1,"1",7,"2",30,"3",3650);//过期时间映射表
}
