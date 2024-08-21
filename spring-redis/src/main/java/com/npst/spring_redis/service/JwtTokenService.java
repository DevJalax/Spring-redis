package com.npst.spring_redis.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.npst.spring_redis.entity.JwtToken;
import com.npst.spring_redis.repo.JwtTokenRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Service
public class JwtTokenService {

	    @Autowired
	    private JwtTokenRepository jwtTokenRepository;
	
	    @Autowired
	    private RedisTemplate<Long, String> redisTemplate;
	    
	    @Value("${spring.secret.key}")
	    private String SECRET_KEY;
	    
	    @Value("${spring.token.expiration}")
	    private long expiration;
	    
	    
	    @Async // Enable asynchronous processing
	    public void createToken(String userName, String password) {
	        System.out.println("Entered createToken() method");
	        
	        long startTime = System.currentTimeMillis(); // Start timing
	        try {
	            // Generate JWT token
	            String jwtToken = generateJwtToken(userName);

	            // Save token entity and send to Redis asynchronously
	            saveTokenToDatabaseAndRedis(jwtToken);
	            
	            long endTime = System.currentTimeMillis(); // End timing
	            System.out.println("Time taken to produce and store token in Redis: " + (endTime - startTime) + " ms");
	        } catch (Exception e) {
	            e.printStackTrace(); // Handle the exception as needed
	        }
	    }

	    private String generateJwtToken(String userName) {
	        // JWT generation logic
	        Instant now = Instant.now();
	        Date issuedAt = Date.from(now);
	        Instant expirationInstant = now.plusMillis(expiration * 1000); // Convert to milliseconds
	        Date expirationDate = Date.from(expirationInstant);

	        return Jwts.builder()
	                   .setSubject(userName)
	                   .setIssuedAt(issuedAt)
	                   .setExpiration(expirationDate)
	                   .signWith(getSignInKey(), SignatureAlgorithm.HS256)
	                   .compact();
	    }

	    private void saveTokenToDatabaseAndRedis(String jwtToken) {
	        // Create and save token entity
	        JwtToken tokenEntity = new JwtToken();
	        tokenEntity.setJwtToken(jwtToken);
	        tokenEntity.setStatus(0); // Initially inactive

	        // Save token entity to the database
	        JwtToken savedToken = jwtTokenRepository.save(tokenEntity);

	        // Send token to Redis asynchronously
	        redisTemplate.opsForValue().set(savedToken.getId(), jwtToken, expiration, TimeUnit.SECONDS);
	    }

	    private Key getSignInKey() {
	        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
	        return Keys.hmacShaKeyFor(keyBytes);
	    }

	    @Async // Enable asynchronous processing
	    public void pushToRedis(List<JwtToken> tokens) {
	        System.out.println("Inside pushToRedis()");
	        long startTime = System.currentTimeMillis();

	        // Save tokens to Redis using pipeline
	        saveTokensToRedis(tokens);

	        // Update token status in the database
	        updateTokenStatus(tokens);

	        long endTime = System.currentTimeMillis(); // End timing
	        System.out.println("Time taken to push tokens to Redis: " + (endTime - startTime) + " ms");
	    }

	    private void saveTokensToRedis(List<JwtToken> tokens) {
	        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
	            connection.openPipeline();
	            for (JwtToken token : tokens) {
	                connection.set(longToBytes(token.getId()), token.getJwtToken().getBytes(StandardCharsets.UTF_8), Expiration.seconds(expiration),RedisStringCommands.SetOption.UPSERT);
	            }
	            connection.closePipeline();
	        }
	    }

	    private byte[] longToBytes(Long value) {
	        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
	    }

	    private void updateTokenStatus(List<JwtToken> tokens) {
	        for (JwtToken token : tokens) {
	            token.setStatus(1);
	        }
	        jwtTokenRepository.saveAll(tokens);
	    }

	    public void pullFromRedis() {
	        System.out.println("Inside pull from redis()");
	        long startTime = System.currentTimeMillis(); // Start timing
	        
	        // Use a more efficient scan method
	        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
	            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").build());

	            // Iterate over the scanned keys and retrieve their values
	            while (cursor.hasNext()) {
	                byte[] key = cursor.next();
	                String value = redisTemplate.opsForValue().get(new String(key, StandardCharsets.UTF_8));
	                System.out.println("Key: " + new String(key, StandardCharsets.UTF_8) + ", Value: " + value);
	            }
	        }

	        long endTime = System.currentTimeMillis(); // End timing
	        System.out.println("Time taken to pull tokens from Redis: " + (endTime - startTime) + " ms");
	    }
	    
	    
	    // Method to pull odd keys from Redis
	    public void pullOddKeysFromRedis() {
	        System.out.println("Inside pull odd keys from redis()");
	        long startTime = System.currentTimeMillis(); // Start timing

	        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
	            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").build());

	            // Iterate over the scanned keys and retrieve their values
	            while (cursor.hasNext()) {
	                byte[] key = cursor.next();
	                String keyString = new String(key, StandardCharsets.UTF_8);
	                
	                // Check if the key is odd (assuming keys are numeric strings)
	                if (isOddKey(keyString)) {
	                    String value = redisTemplate.opsForValue().get(keyString);
	                    System.out.println("Odd Key: " + keyString + ", Value: " + value);
	                }
	            }
	        }

	        long endTime = System.currentTimeMillis(); // End timing
	        System.out.println("Time taken to pull odd keys from Redis: " + (endTime - startTime) + " ms");
	    }

	    // Method to pull even keys from Redis
	    public void pullEvenKeysFromRedis() {
	        System.out.println("Inside pull even keys from redis()");
	        long startTime = System.currentTimeMillis(); // Start timing

	        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
	            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("*").build());

	            // Iterate over the scanned keys and retrieve their values
	            while (cursor.hasNext()) {
	                byte[] key = cursor.next();
	                String keyString = new String(key, StandardCharsets.UTF_8);
	                
	                // Check if the key is even (assuming keys are numeric strings)
	                if (isEvenKey(keyString)) {
	                    String value = redisTemplate.opsForValue().get(keyString);
	                    System.out.println("Even Key: " + keyString + ", Value: " + value);
	                }
	            }
	        }

	        long endTime = System.currentTimeMillis(); // End timing
	        System.out.println("Time taken to pull even keys from Redis: " + (endTime - startTime) + " ms");
	    }

	    // Helper method to determine if a key is odd
	    private boolean isOddKey(String key) {
	        try {
	            return Integer.parseInt(key) % 2 != 0; // Check if the key is odd
	        } catch (NumberFormatException e) {
	            return false; // Handle non-numeric keys
	        }
	    }

	    // Helper method to determine if a key is even
	    private boolean isEvenKey(String key) {
	        try {
	            return Integer.parseInt(key) % 2 == 0; // Check if the key is even
	        } catch (NumberFormatException e) {
	            return false; // Handle non-numeric keys
	        }
	    }
	    
	    public List<JwtToken> getAllTokens() {
	        return jwtTokenRepository.findAll();
	    }

	    public void logout(Long id) {
	        jwtTokenRepository.deleteById(id);
	        redisTemplate.delete(id);
	    }
}
