package com.npst.spring_redis.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
public class RedisConfig {

	    @Bean
	    public JedisConnectionFactory jedisConnectionFactory() {
	        JedisConnectionFactory factory = new JedisConnectionFactory();
	        factory.setHostName("redis");
	        factory.setPort(6379);
	        factory.getPoolConfig().setMaxTotal(150); // Maximum number of connections
	        factory.getPoolConfig().setMaxIdle(100);   // Maximum idle connections
	        factory.getPoolConfig().setMinIdle(50);   // Minimum idle connections
	        return factory;
	    }
	
	@Bean
	public RedisTemplate<Long, String> redisTemplate() {
	    RedisTemplate<Long, String> template = new RedisTemplate<>();
	    template.setConnectionFactory(jedisConnectionFactory());
	    template.setKeySerializer(new GenericToStringSerializer<>(Long.class)); // Use GenericToStringSerializer for Long keys
	    template.setValueSerializer(new StringRedisSerializer()); // Use StringRedisSerializer for String values
	    return template;
	}
}
