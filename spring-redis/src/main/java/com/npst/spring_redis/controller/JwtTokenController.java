package com.npst.spring_redis.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.npst.spring_redis.dto.Request;
import com.npst.spring_redis.entity.JwtToken;
import com.npst.spring_redis.service.JwtTokenService;

@RestController
@RequestMapping("/api/tokens")
public class JwtTokenController {

     @Autowired
	 private JwtTokenService jwtTokenService;
	
     private final ExecutorService executorService = Executors.newFixedThreadPool(20);
     
     @PostMapping("/generate")
     public void generateTokens(@RequestParam int count , @RequestBody Request loginRequest) {
         for (int i = 0; i < count; i++) {
             executorService.submit(() -> {
                 jwtTokenService.createToken(loginRequest.getUsername(),loginRequest.getPassword());
             });
         }
     }
     
     @GetMapping("/")
     public List<JwtToken> getAllTokens() {
         return jwtTokenService.getAllTokens();
     }

     @DeleteMapping("/{id}")
     public void logout(@PathVariable Long id) {
         jwtTokenService.logout(id);
     }
}
