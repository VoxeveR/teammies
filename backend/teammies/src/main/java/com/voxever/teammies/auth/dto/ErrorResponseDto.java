package com.voxever.teammies.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponseDto {
    private long timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}