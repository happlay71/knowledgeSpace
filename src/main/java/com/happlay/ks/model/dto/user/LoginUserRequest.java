package com.happlay.ks.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserRequest {
    // 不为空
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
