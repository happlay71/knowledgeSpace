package com.happlay.ks.emums;

public enum FileTypeEnum {
    AVATAR("avatar"),  // 头像文件
    FILE("file"),  // 文档文件，例如包含文字的文件
    DOCUMENT("document");  // 用于区分创建的文件类型，如md文档
    private final String type;

    FileTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
