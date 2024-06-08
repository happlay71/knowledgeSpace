package com.happlay.ks.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetUserPasswordRequest {
    private String password;
    private String password2;
}
