package com.project.educonnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.project.educonnect.dto.LeetCodeStats;

@Service
public class LeetCodeService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String URL =
            "https://leetcode.com/graphql";

    public LeetCodeStats getStats(String username) {

        String query = """
        query getUserProfile($username: String!) {
          matchedUser(username: $username) {
            submitStats {
              acSubmissionNum {
                difficulty
                count
              }
            }
            profile {
              ranking
            }
          }
        }
        """;

        Map<String, Object> variables =
                new HashMap<>();

        variables.put("username", username);

        Map<String, Object> body =
                new HashMap<>();

        body.put("query", query);
        body.put("variables", variables);

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        URL,
                        HttpMethod.POST,
                        entity,
                        Map.class);

        Map data =
                (Map) response.getBody().get("data");

        Map matchedUser =
                (Map) data.get("matchedUser");

        if (matchedUser == null) {
            return null;
        }

        Map submitStats =
                (Map) matchedUser.get("submitStats");

        List<Map<String, Object>> stats =
                (List<Map<String, Object>>)
                        submitStats.get("acSubmissionNum");

        LeetCodeStats result =
                new LeetCodeStats();

        for (Map<String, Object> item : stats) {

            String difficulty =
                    (String) item.get("difficulty");

            Integer count =
                    (Integer) item.get("count");

            switch (difficulty) {

                case "All":
                    result.setTotalSolved(count);
                    break;

            }
        }

        return result;
    }
}