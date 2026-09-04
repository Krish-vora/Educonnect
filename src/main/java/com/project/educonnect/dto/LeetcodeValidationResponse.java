package com.project.educonnect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeetcodeValidationResponse {

    private boolean valid;
    private String message;
}