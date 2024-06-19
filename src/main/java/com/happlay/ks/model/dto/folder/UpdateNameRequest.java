package com.happlay.ks.model.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNameRequest {
    private Integer id;
    private String name;
}
