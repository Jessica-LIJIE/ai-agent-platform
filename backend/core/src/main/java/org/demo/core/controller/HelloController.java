package org.demo.core.controller;

import org.demo.core.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/hello")
@RestController
public class HelloController {

    @GetMapping
    public ApiResponse<String> hello() {
        return ApiResponse.ok("hello");
    }
}
