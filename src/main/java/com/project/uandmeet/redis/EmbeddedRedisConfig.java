package com.project.uandmeet.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * <h1>내장 레디스 설정</h1>
 * <p>스프링 내장 레디스 설정 클래스</p>
 * <p>로컬에서 테스트 하려면 프로필을 수정하면 된다</p>
 */
@Profile("local")
// @Profile("${spring.redis.host}")
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            // redisServer = new RedisServer(redisPort);
            redisServer = RedisServer.builder()
            .port(redisPort)
            .setting("maxmemory 128M")
            .build();
            redisServer.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start embedded Redis", e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}

