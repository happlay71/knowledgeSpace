package com.happlay.ks.model.vo.avatar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadVo {
    private int userId;
    private String avatarUrl;
}
