package com.happlay.ks.model.dto.file;

import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileRequest {
    private MultipartFile file;
    private FileTypeEnum fileType = FileTypeEnum.FILE;
    private Integer folderId;
    private String name;
}
