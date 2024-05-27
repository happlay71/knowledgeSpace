package com.happlay.ks.model.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserVo {
    private Integer id;
    private String username;
    private String role;
    private String token;
}
