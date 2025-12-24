package com.example.ordermanager.health;

import com.example.ordermanager.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping(value = {"/","/ok","/health"})
    public ApiResponse<String> health() {
        return ApiResponse.success("OK");
    }
}
