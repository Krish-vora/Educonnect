// ======================
// FILE: GithubService.java
// ======================

package com.project.educonnect.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GithubService {

    public int getGithubFollowers(String githubUsername) {

        try {

            String url =
                    "https://api.github.com/users/" + githubUsername;

            RestTemplate restTemplate =
                    new RestTemplate();

            ResponseEntity<Map> response =
                    restTemplate.getForEntity(url, Map.class);

            Map body = response.getBody();

            if (body != null &&
                    body.get("followers") != null) {

                return (Integer) body.get("followers");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }
}