package com.happlay.ks.model.vo.user;

import com.happlay.ks.model.vo.file.FileDetailsVo;
import com.happlay.ks.model.vo.folder.FolderDetailsVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsVo {
    private Integer id;
    private String username;
    private String role;
    private String avatarUrl;
    private FolderDetailsVo folders;
}
