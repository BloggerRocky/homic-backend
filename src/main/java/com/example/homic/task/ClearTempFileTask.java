package com.example.homic.task;

import com.example.homic.utils.MinioUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/16.23:08
 * 项目名：homic
 */
//用于处理清除临时文件的定时任务
@Component
@Data
public class ClearTempFileTask implements SchedulingConfigurer {
    @Autowired
    MinioUtils minioUtils;
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                //重写run()
                new Runnable() {public void run() {
                    try {
                        //清楚临时文件夹
                        minioUtils.deleteFolder("temp");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }},
                //重写nextExecutionTime()
                new Trigger() {public Date nextExecutionTime(TriggerContext triggerContext) {
                    //每天三点钟执行一次
                    return new CronTrigger("0 0 3 * * ? ").nextExecutionTime(triggerContext);
                }}
        );

    }
}
