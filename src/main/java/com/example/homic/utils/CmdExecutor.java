package com.example.homic.utils;

import com.example.homic.exception.MyException;
import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/17.13:30
 * 项目名：homic
 */
public class CmdExecutor {
    private static Logger logger = LoggerFactory.getLogger(CmdExecutor.class);

    /**
     * 执行一个外部命令并返回其输出结果。
     * @param commands 要执行的命令数组
     * @return 命令执行后的输出结果
     */
    public static void executeCommand(List<String> commands) throws IOException, InterruptedException {
        try {
            // 创建 ProcessBuilder 实例
            ProcessBuilder pb = new ProcessBuilder(commands);
            // 启动进程
            Process process = pb.start();
            // 读取输出(要分别【先后】读取错误流和输出流，不然一些程序可能会发生阻塞）
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            InputStreamReader errorStreamReader = new InputStreamReader(errorStream, "GBK");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
            String line;
            BufferedReader reader = new BufferedReader(errorStreamReader);
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
            }
            reader = new BufferedReader(inputStreamReader);
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
            }
            // 等待进程结束
            int exitCode = process.waitFor();
            reader.close();
            inputStreamReader.close();
            inputStream.close();
            if(exitCode != 0)
                {
                logger.info("CMD程序执行失败: " + exitCode);
                throw new Exception("CMD程序执行失败: " + exitCode);
                }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
