package com.ihome.controller;

import com.ihome.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public ApiResponse<String> root() {
        return ApiResponse.ok("IHome backend is running. See /swagger-ui.html for API docs.");
    }

}


