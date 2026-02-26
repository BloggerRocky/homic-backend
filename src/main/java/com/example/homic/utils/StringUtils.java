package com.example.homic.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/11.22:46
 * 项目名：homic
 */
//修改，检验，获取随机字符串的工具类
public class StringUtils {

    public static final boolean isEmpty(String str)
    {
        if(str == null || str.equals(""))
            return true;
        return false;
    }
    //获取一个随机数字串
    public static final String getRandomNumber(Integer count)
    {
        return RandomStringUtils.random(count,false,true);
    }
    //获取一个随机字母数字字符串
    public static final String getSerialNumber(Integer count)
    {
        return RandomStringUtils.random(count,true,true);
    }
    //获取一个随机字母串
    public static final String getRandomLetter(Integer count)
    {
        return RandomStringUtils.random(count,true,false);
    }
    //md5加密
    public static final String getMD5(String str)
    {
        return DigestUtils.md5Hex(str);
    }
    //sha1加密
    public static final String getSHA1(String str)
    {
        return DigestUtils.sha1Hex(str);
    }
    //getSHA256加密
    public static final String getSHA256(String str)
    {
        return DigestUtils.sha256Hex(str);
    }
    //将Date对象转化成对应格式的字符串
    public static final String formatDate(Date date,String pattern)
    {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
        String formattedDate = dateFormatter.format(date);
        return formattedDate;
    }

}
