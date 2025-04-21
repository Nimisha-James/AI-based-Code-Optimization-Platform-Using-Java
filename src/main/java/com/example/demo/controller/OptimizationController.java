package com.example.demo.controller;

import com.example.demo.service.CodeOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/code")
@CrossOrigin(origins = "*")
public class OptimizationController {

    @Autowired
    private CodeOptimizationService codeOptimizationService;

    @PostMapping("/optimize")
    public Map<String, Object> optimizeCode(@RequestBody Map<String, String> request) {
        String userEmail = request.get("email");
        String code = request.get("code");

        if (userEmail == null || code == null || userEmail.isEmpty() || code.isEmpty()) {
            return Map.of("success", false, "error", "Email and Code are required!");
        }

        try {
            Map<String, String> result = codeOptimizationService.optimizeCode(code, userEmail);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("optimizedCode", result.get("optimizedCode"));
            response.put("complexity", result.get("complexity"));
            response.put("message", "Code optimized and stored successfully!");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to optimize code: " + e.getMessage());
            return response;
        }
    }
}