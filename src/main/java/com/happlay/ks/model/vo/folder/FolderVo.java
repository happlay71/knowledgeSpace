package com.happlay.ks.model.vo.folder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderVo {
    private Integer id;
    private String name;
    private List<FolderVo> subFolders;

}
