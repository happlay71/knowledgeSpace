package com.happlay.ks.model.vo.file;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDetailsVo {
    private Integer id;
    private String name;
    private String path;
    private String fileType;
}
