package com.voxever.teammies.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank(message = "Email cannot be blank!")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Password cannot be blank!")
    @JsonProperty("password")
    private String password;
}