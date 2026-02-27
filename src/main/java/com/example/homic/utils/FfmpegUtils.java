package com.example.homic.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/17.13:10
 * 项目名：homic
 */
//用于操作ffmpeg的工具类
@Slf4j
public class FfmpegUtils {
    //制作视频封面（缩略图）
    public static void createVideoCover(String filePath, String targetPath) throws Exception {
        try {
            List<String> commands = new ArrayList<>();
            commands.add("ffmpeg");
            commands.add("-i");
            commands.add(filePath);
            commands.add("-y");
            commands.add("-ss");
            commands.add("00:00:01");
            commands.add("-vframes");
            commands.add("1");
            commands.add("-vf");
            commands.add("scale='min(640,iw)':'min(480,ih)'");
            commands.add(targetPath);
            CmdExecutor.executeCommand(commands);
        } catch (Exception e) {
            log.error("为视频生成封面时发生错误");
            throw e;
        }
    }

    //制作图片缩略图
    public static void createImageCover(String filePath, String targetPath) throws Exception {
        try {
            List<String> commands = new ArrayList<>();
            commands.add("ffmpeg");
            commands.add("-i");
            commands.add(filePath);
            commands.add("-y");
            commands.add("-vf");
            commands.add("scale='min(640,iw)':'min(480,ih)'");
            commands.add(targetPath);
            CmdExecutor.executeCommand(commands);
        } catch (Exception e) {
            log.error("为图片生成封面时发生错误");
            throw e;
        }
    }

    //将视频转码成ts格式
    public static void turnVideo2Ts(String videoPath, String tsPath) throws IOException, InterruptedException {
        try {
            //生成cmd命令
            List<String> commands = new ArrayList<>();
            commands.add("ffmpeg");
            commands.add("-y");//如果运行过程中有键入，直接输入y或回车
            commands.add("-i");
            commands.add(videoPath);//装填原文件路径
            commands.add("-c:v");
            commands.add("copy");
            commands.add("-c:a");
            commands.add("copy");
            commands.add("-bsf:v");
            commands.add("h264_mp4toannexb");
            commands.add("-f");
            commands.add("mpegts");//指定输出文件类型
            commands.add(tsPath);//装填输出文件路径
            //执行cmd命令
            CmdExecutor.executeCommand(commands);
        } catch (Exception e) {
            log.error("视频转码为ts文件时转码失败");
            throw e;
        }
    }

    //对TS视频进行切割
    public static void cutTsVedio(String tsPath, String m3u8Path, String cuttingPath) throws IOException, InterruptedException {
        try {
            //生成cmd命令
            List<String> commands = new ArrayList<>();
            commands.add("ffmpeg");
            commands.add("-i");
            commands.add(tsPath);//装填ts原文件路径
            commands.add("-c");
            commands.add("copy");
            commands.add("-map");
            commands.add("0");
            commands.add("-f");
            commands.add("segment");
            commands.add("-segment_list");
            commands.add(m3u8Path);//指定m3u8文件路径
            commands.add("-segment_time");
            commands.add("15");
            commands.add(cuttingPath + "/" + "%04d.ts");
            //执行cmd命令
            CmdExecutor.executeCommand(commands);
        } catch (Exception e) {
            log.error("视频转码为ts文件时转码失败");
            throw e;
        }
    }
}
