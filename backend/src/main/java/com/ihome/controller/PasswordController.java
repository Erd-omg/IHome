package com.ihome.controller;

import com.ihome.common.ApiResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
public class PasswordController {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @PostMapping("/hash")
    public ApiResponse<String> generateHash(@RequestBody PasswordRequest request) {
        String hash = passwordEncoder.encode(request.password);
        return ApiResponse.ok(hash);
    }
    
    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyPassword(@RequestBody PasswordRequest request) {
        boolean matches = passwordEncoder.matches(request.password, request.hash);
        return ApiResponse.ok(matches);
    }
    
    public static class PasswordRequest {
        public String password;
        public String hash;
    }
}










