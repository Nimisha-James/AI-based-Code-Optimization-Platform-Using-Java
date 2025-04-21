package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import com.example.demo.service.CodeParser;

@CrossOrigin(origins = "http://localhost:8081")  // Allow frontend
@RestController
@RequestMapping("/api")
public class CodeCheckController {
    private final CodeParser codeParser;

    public CodeCheckController(CodeParser codeParser) {
        this.codeParser = codeParser;
    }

    @PostMapping("/check")
    public Map<String, Object> checkCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        List<String> errors = codeParser.parse(code);

        return Map.of(
            "valid", errors.isEmpty(),
            "errors", errors,
            "stats", Map.of(
                "lexicalErrors", errors.stream().filter(e -> e.contains("Illegal character")).count(),
                "syntaxErrors", errors.stream().filter(e -> e.contains("missing") || e.contains("extraneous")).count(),
                "semanticErrors", errors.stream().filter(e -> e.contains("Type mismatch") || e.contains("Undefined")).count()
            )
        );
    }
}


