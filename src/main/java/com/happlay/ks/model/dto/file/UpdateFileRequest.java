package com.happlay.ks.model.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFileRequest {
    private Integer Id;
    private String content;
}
