package kz.narxoz.redis.middle02redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Middle02redisApplication {
    public static void main(String[] args) {
        SpringApplication.run(Middle02redisApplication.class, args);
    }
}
