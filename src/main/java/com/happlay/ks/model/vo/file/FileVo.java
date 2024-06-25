package com.happlay.ks.model.vo.file;

import com.happlay.ks.model.vo.user.UserFileVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileVo {
    private Integer id;
    private UserFileVo userFileVo;
    private String name;
    private String fileType;
}
