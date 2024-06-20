package com.happlay.ks.model.dto.file;

import com.happlay.ks.emums.FileTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFileRequest {
    private String fileName;
    private String content;
    private FileTypeEnum fileType;
    private Integer folderId;
}
