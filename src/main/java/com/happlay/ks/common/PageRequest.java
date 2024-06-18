package com.happlay.ks.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest {
    /**
     * 当前页数
     */
    private long current;

    /**
     * 每页数据数量
     */
    private long pageSize;
}
