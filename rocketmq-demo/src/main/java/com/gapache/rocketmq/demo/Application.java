package com.gapache.rocketmq.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.UUID;

/**
 * @author HuSen
 * @since 2021/3/19 2:38 下午
 */
@Slf4j
@SpringBootApplication
@EnableBinding({Source.class, Sink.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ApplicationRunner runner(Source source) {
        return args -> source.output().send(
                MessageBuilder.withPayload("这是王杰")
                        .setHeader(RocketMQHeaders.TAGS, "tagA")
                        .setHeader(RocketMQHeaders.KEYS, UUID.randomUUID().toString())
                        .build()
        );
    }

    @StreamListener(Sink.INPUT)
    public void getMessage(Message<String> messageBody) {
        System.out.println("接收到新的消息体: " + messageBody);
    }
}
