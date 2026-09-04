package com.project.educonnect.controller;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.educonnect.dto.LeetcodeValidationResponse;

@RestController
@RequestMapping("/api/coding")
public class CodingProfileController {

    private final WebClient webClient = WebClient.create();

    @GetMapping("/validate/leetcode")
    public ResponseEntity<?> validateLeetcode(
            @RequestParam String username) {

        try {

            String query = """
                    query getUserProfile($username: String!) {
                        matchedUser(username: $username) {
                            username
                        }
                    }
                    """;

            Map<String, Object> body = Map.of(
                    "query", query,
                    "variables", Map.of("username", username));

            Map response = webClient.post()
                    .uri("https://leetcode.com/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map data = (Map) response.get("data");

            Object matchedUser = data.get("matchedUser");

            if (matchedUser != null) {

                return ResponseEntity.ok(
                        new LeetcodeValidationResponse(
                                true,
                                "Valid LeetCode username"));

            }

            return ResponseEntity.ok(
                    new LeetcodeValidationResponse(
                            false,
                            "Username not found"));

        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(new LeetcodeValidationResponse(
                            false,
                            "Validation failed"));
        }
    }
}