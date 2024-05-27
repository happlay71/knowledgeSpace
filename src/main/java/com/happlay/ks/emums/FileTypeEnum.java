package com.happlay.ks.emums;

public enum FileTypeEnum {
    AVATAR("avatar"),  // 头像文件
    FILE("file");  // 文档文件，例如包含文字的文件

    private final String type;

    FileTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
