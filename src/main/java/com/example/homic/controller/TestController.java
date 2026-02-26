package com.example.homic.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/10.18:29
 * 项目名：homic
 */
@RestController
public class TestController {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @GetMapping("test")
    public String test(){
        rabbitTemplate.convertAndSend("merge_exchange","merge","你好，我才打印出来");
        return "6666666666";
    }

}
