package com.happlay.ks.model.vo.folder;

import com.happlay.ks.model.vo.file.FileDetailsVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderDetailsVo {
    private Integer id;
    private String name;
    private List<FolderDetailsVo> subFolders;
    private List<FileDetailsVo> files;
}
