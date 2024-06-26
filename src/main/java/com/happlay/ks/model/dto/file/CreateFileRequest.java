package com.happlay.ks.model.dto.file;

import com.happlay.ks.emums.FileTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFileRequest {
    private String name;
    private String content;
    private Integer folderId;
}
