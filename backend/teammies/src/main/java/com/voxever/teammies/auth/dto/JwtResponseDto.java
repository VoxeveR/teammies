package com.voxever.teammies.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDto {

    @JsonProperty("access_token_type")
    private String accessTokenType;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("access_token_expires_in")
    private long accessTokenExpiresIn;

    @JsonProperty("username")
    private String username;

}