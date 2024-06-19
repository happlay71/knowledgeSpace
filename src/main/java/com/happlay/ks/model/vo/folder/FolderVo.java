package com.happlay.ks.model.vo.folder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderVo {
    private Integer id;
    private Integer parentId;
    private String name;

}
