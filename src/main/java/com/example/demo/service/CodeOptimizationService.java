package com.example.demo.service;

import com.example.demo.model.CodeEntry;
import com.example.demo.repository.CodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class CodeOptimizationService {

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private TimeComplexityService timeComplexityService;

    // ✅ Use Gemini 2.0 Pro Experimental model
    private static final String GEMINI_API_KEY = "AIzaSyDquw-wRq3fN8Xnms_Lpd6UKRaK0UB_NxA";
    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-pro-exp:generateContent?key=" + GEMINI_API_KEY;

    public Map<String, String> optimizeCode(String code, String userEmail) {
        Map<String, String> result = new HashMap<>();

        try {
            String escapedCode = code.replace("\"", "\\\"");

            System.out.println("📤 Preparing Gemini Pro prompt...");

            // ✅ Payload format for Gemini Pro
            String requestBody = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "You are a Java assistant. Optimize this code and return only the Java code:\\n\\n%s"
                    }]
                  }]
                }
            """.formatted(escapedCode);

            System.out.println("📡 Sending request to Gemini Pro...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("✅ Gemini responded with status code: " + response.statusCode());
            System.out.println("📦 Gemini raw response:\n" + response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("❌ Gemini API call failed: HTTP " + response.statusCode());
            }

            // ✅ Parse response for Gemini Pro
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            String optimizedCode = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            // ✅ Clean up markdown formatting
            optimizedCode = optimizedCode.replaceAll("(?s)(?:java)?\\s*", "").replaceAll("", "").trim();

            System.out.println("🧠 Optimized code before complexity analysis:\n" + optimizedCode);

            // ✅ Analyze time complexity
            String complexity;
            try {
                complexity = timeComplexityService.analyzeAndStoreComplexity(optimizedCode, userEmail);
            } catch (Exception e) {
                e.printStackTrace();
                complexity = "Error analyzing complexity";
            }

            // ✅ Save to MongoDB
            CodeEntry entry = new CodeEntry(userEmail, code, complexity, optimizedCode);
            codeRepository.save(entry);

            result.put("optimizedCode", optimizedCode);
            result.put("complexity", complexity);

        } catch (Exception e) {
            System.err.println("🚨 Exception during optimization: " + e.getMessage());
            e.printStackTrace();
            result.put("optimizedCode", "Error during optimization");
            result.put("complexity", "Error");
        }

        return result;
    }
}