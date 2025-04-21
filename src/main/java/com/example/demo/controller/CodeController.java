package com.example.demo.controller;

import com.example.demo.service.TimeComplexityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/code")
@CrossOrigin(origins = "*")  // Allow frontend to access API
public class CodeController {

    @Autowired
    private TimeComplexityService timeComplexityService;

    @PostMapping("/analyze")
    public Map<String, Object> analyzeCode(@RequestBody Map<String, String> request) {
        String userEmail = request.get("email");
        String code = request.get("code");

        if (userEmail == null || code == null || userEmail.isEmpty() || code.isEmpty()) {
            return Map.of("success", false, "error", "Email and Code are required!");
        }

        try {
            // Store code and return complexity
            String complexity = timeComplexityService.analyzeAndStoreComplexity(code, userEmail);
            
            // Proper response structure for frontend
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("complexity", complexity);
            response.put("message", "Code stored successfully!");
            return response;
        } catch (Exception e) {
            // Handle any exception (e.g., parsing errors, database issues)
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to analyze code: " + e.getMessage());
            return response;
        }
    }
}