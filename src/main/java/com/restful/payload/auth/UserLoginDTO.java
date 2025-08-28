package com.restful.payload.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserLoginDTO {

    @Schema(description = "email", example = "gaurav10@gmail.com")
    private String email;

    @Schema(description = "password", example = "123456")
    private String password;
}
