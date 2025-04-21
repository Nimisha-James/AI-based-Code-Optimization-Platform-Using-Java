package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Sign Up Endpoint
    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            response.put("success", "false");
            response.put("message", "User already exists!");
        } else {
            userRepository.save(user);
            response.put("success", "true");
            response.put("message", "User registered successfully!");
        }
        return response;
    }

    // Sign In Endpoint
    @PostMapping("/signin")
    public Map<String, String> signin(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        
        // Check if the email exists in the database
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            // Verify the password matches the stored password
            if (existingUser.get().getPassword().equals(user.getPassword())) {
                response.put("success", "true");
                response.put("message", "Login successful!");
            } else {
                response.put("success", "false");
                response.put("message", "Incorrect password!");
            }
        } else {
            response.put("success", "false");
            response.put("message", "User not registered!");
        }
        return response;
    }
}