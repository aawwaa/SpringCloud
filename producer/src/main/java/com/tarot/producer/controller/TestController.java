package com.tarot.producer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaohejia
 * @version 1.0.0
 * @date 2019/10/25
 */
@RefreshScope
@RestController
public class TestController {

    @Value(value = "${content}")
    public String content;

    /**
     * http://localhost:8002/test
     * @return
     */
    @GetMapping("/test")
    public String test(){
        return content;
    }

}
