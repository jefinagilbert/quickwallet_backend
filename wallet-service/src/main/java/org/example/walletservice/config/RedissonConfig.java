package org.example.walletservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class RedissonConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + redisHost + ":" + redisPort;

        var singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20)
                .setConnectTimeout(3000)
                .setTimeout(3000);

        if (redisPassword != null && !redisPassword.isBlank()) {
            singleServerConfig.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
