package com.example.homic.config.listeners;

import com.alibaba.fastjson.JSON;
import com.example.homic.dto.rabbitMQ.MergeMessageDTO;
import com.example.homic.services.FileService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/16.18:22
 * 项目名：homic
 */
//合并文件消息监听器
@Component
@Slf4j
public class MergeMsgListener {
    //生者消息监听器经由交换机发布消息，再根据不同的路由键将消息发送到不同的队列中
    //如果在Console端已经配置好了队列，交换机，路由键的关系，那么监听注解上只用绑定队列即可
    public static final String EXCHANGE_NAME = "merge_exchange";//交换机名称
    public static final String ROUTING_KEY = "merge";//路由键（该监听器能接收merge路由键的消息）
    public static final String QUEUE_NAME = "merge_queue";//队列名称
    @Autowired
    FileService fileService;
    Logger logger = LoggerFactory.getLogger(MergeMsgListener.class);
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QUEUE_NAME, durable = "true"),//绑定队列频道
            exchange = @Exchange(value = EXCHANGE_NAME),//绑定交换机名称
            key = {ROUTING_KEY}//绑定路由键（路由键可以绑定多个）
    ))
    public void processMessage(String dataString, Message message, Channel channel) throws Exception {
        //获取消息唯一表示符号
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //将字符串反序列化成对应包装类
            MergeMessageDTO messageDTO = JSON.parseObject(dataString, MergeMessageDTO.class);
            fileService.mergeFile(messageDTO.getUploadDTO(),messageDTO.getFilePath(),messageDTO.getUserId());
        } catch (Exception e) {
            //参数解释：b:若为false:仅拒绝deliveryTag指定的消息，若为true:拒绝所有未确认的消息
            //b1:若为false,不将拒绝的信息重新入队，反之入队
            channel.basicNack(deliveryTag,false,false);
            logger.error("合并文件监听器执行任务失败");
            throw e;
        }

    }
}
