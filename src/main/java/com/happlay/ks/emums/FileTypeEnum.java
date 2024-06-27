package com.happlay.ks.emums;

public enum FileTypeEnum {
    AVATAR("avatar"),  // 头像文件
    PHOTO("document/photo"),  // 图片文件
    DOCUMENT("document");  // 文档文件

    private final String type;

    FileTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static FileTypeEnum fromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return PHOTO;  // 图片文件类型
            case "md":
            case "pdf":
            case "doc":
            case "docx":
                return DOCUMENT;  // 文档文件类型
            default:
                throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }
}
