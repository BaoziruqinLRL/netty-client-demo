package com;

import com.alibaba.fastjson.JSON;
import com.client.LeadClientHandler;
import com.data.TransferData;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@SpringBootApplication
@RestController
@RequestMapping(value = "/client")
public class NettyClientDemoApplication implements CommandLineRunner {

    @Resource
    private LeadClientHandler leadClientHandler;

    public static void main(String[] args) {
        SpringApplication.run(NettyClientDemoApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        leadClientHandler.start();
    }

    @RequestMapping(value = "/send")
    public void send(@RequestParam("clientId") String clientId){
        var transferData = new TransferData();
        transferData.setType("accept");
        transferData.setContent(clientId);
        transferData.setAck(leadClientHandler.buildAck());
        leadClientHandler.send(JSON.toJSONString(transferData));
    }
}
