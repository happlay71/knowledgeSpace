package com.happlay.ks.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.happlay.ks.common.ErrorCode;
import com.happlay.ks.constant.UserRoleConstant;
import com.happlay.ks.emums.FileTypeEnum;
import com.happlay.ks.exception.CommonException;
import com.happlay.ks.mapper.FolderMapper;
import com.happlay.ks.model.dto.file.CreateFileRequest;
import com.happlay.ks.model.dto.file.UpdateFileRequest;
import com.happlay.ks.model.dto.file.UpdateNameRequest;
import com.happlay.ks.model.dto.file.UploadFileRequest;
import com.happlay.ks.model.entity.File;
import com.happlay.ks.mapper.FileMapper;
import com.happlay.ks.model.entity.Folder;
import com.happlay.ks.model.entity.User;
import com.happlay.ks.model.vo.file.FileDetailsVo;
import com.happlay.ks.model.vo.folder.FolderDetailsVo;
import com.happlay.ks.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.happlay.ks.service.IFolderService;
import com.happlay.ks.service.IImagepathsService;
import com.happlay.ks.utils.file.FileImageUtils;
import com.happlay.ks.utils.file.FileUtils;
import com.happlay.ks.utils.file.FolderUtils;
import com.happlay.ks.utils.imagepaths.ImageUtils;
import io.swagger.models.auth.In;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author happlay
 * @since 2024-05-23
 */
@Service
@Transactional
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    @Resource
    FileUtils fileUtils;

    @Resource
    FileImageUtils fileImageUtils;

    @Resource
    FolderUtils folderUtils;

    @Resource
    ImageUtils imageUtils;

    @Resource
    FolderMapper folderMapper;

    @Resource
    FileMapper fileMapper;

    @Override
    public String uploadFile(UploadFileRequest uploadFileRequest, User user) {
        // 检查文件夹是否存在并属于当前用户
        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                || Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN)
                || Objects.equals(user.getRole(), UserRoleConstant.ROOT))
                && folderMapper.countFolderBelongsToUser(uploadFileRequest.getFolderId(), user.getId()) == 0) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "操作无效, 文件夹不属于当前用户");
        }

        MultipartFile file = uploadFileRequest.getFile();
        String originalFilename = file.getOriginalFilename();

        if (file.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件内容不能为空");
        }

        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持该文件类型");
        }

        FileTypeEnum fileType = FileTypeEnum.fromFileName(originalFilename);

        if (fileType != FileTypeEnum.DOCUMENT) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "仅支持文档文件类型");
        }

        String name = uploadFileRequest.getName();
        Integer folderId = uploadFileRequest.getFolderId();

        if (name.isEmpty()) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getName, name).eq(File::getUserId, user.getId());
        if (this.getOne(queryWrapper) != null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "文件名不能重复");
        }

        String path;
        try {
            if (originalFilename.endsWith(".md")) {
                path = dealMD(file, folderId, user.getId(), name, fileType);
            } else {
                String relativePath = fileUtils.saveFile(file, fileType, folderId);
                System.out.println("普通文件保存完成，路径：" + relativePath);

                // 保存文件记录到数据库
                File newFile = save(folderId, user.getId(), name, fileType);
                newFile.setPath(relativePath);
                this.updateById(newFile);

                // 创建本地对应的图片文件夹
                folderUtils.createFolderFromPath(FileTypeEnum.PHOTO, newFile.getId());

                System.out.println("文件信息保存到数据库，路径：" + relativePath);
                path = relativePath;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonException(ErrorCode.SYSTEM_ERROR, "处理文件失败");
        }

        return path;
    }

    @Override
    public Boolean createMDFile(CreateFileRequest createFileRequest, User user) {
        // 检查文件夹是否存在并属于当前用户
        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                ||  Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN))
                && folderMapper.countFolderBelongsToUser(createFileRequest.getFolderId(), user.getId()) == 0) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "操作无效, 文件夹不属于当前用户");
        }

        // 参数校验
        if (createFileRequest.getName() == null || createFileRequest.getContent() == null || createFileRequest.getFolderId() == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "请求参数无效");
        }

        // 检查文件名在同一文件夹下是否已经存在
        LambdaQueryWrapper<File> fileQueryWrapper = new LambdaQueryWrapper<>();
        fileQueryWrapper.eq(File::getFolderId, createFileRequest.getFolderId())
                .eq(File::getName, createFileRequest.getName())
                .eq(File::getUserId, user.getId());
        if (this.getOne(fileQueryWrapper) != null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "同名文件已存在");
        }

        try {
            // 将内容转换为 MultipartFile 以便调用 dealMD 方法
            MultipartFile multipartFile = new MockMultipartFile(
                    createFileRequest.getName(),
                    createFileRequest.getContent().getBytes(StandardCharsets.UTF_8)
            );

            // 调用 dealMD 方法处理 Markdown 文件
            String relativePath = dealMD(multipartFile, createFileRequest.getFolderId(), user.getId(), createFileRequest.getName(), FileTypeEnum.DOCUMENT);

            if (relativePath == null) {
                throw new CommonException(ErrorCode.OPERATION_ERROR, "文件信息保存失败");
            }

            return true;
        } catch (IOException e) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "文件处理失败" + e);
        }
    }

    @Override
    public Boolean deleteFile(Integer id, User user) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getId, id);
        File file = this.getOne(queryWrapper);

        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                ||  Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN))
                && !Objects.equals(file.getUserId(), user.getId())) {
            throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "无权限删除该文件");
        }

        imageUtils.deleteImage(id);
        folderUtils.deleteFolderById(FileTypeEnum.PHOTO, id);
        fileUtils.deleteFileFromPath(file.getPath());
        fileMapper.deleteById(id);
        return true;
    }

    public Boolean deleteByFolder(Integer folderId, User user) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getFolderId, folderId);
        List<File> files = fileMapper.selectList(queryWrapper);

        files.forEach(file -> {
            deleteFile(file.getId(), user);
        });

        return true;
    }

    @Override
    public String updateFileName(UpdateNameRequest updateNameRequest, User user) {
        // 检查输入参数是否为空
        if (updateNameRequest == null || updateNameRequest.getId() == null || updateNameRequest.getName() == null) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "请求参数无效");
        }

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getId, updateNameRequest.getId());
        File file = this.getOne(queryWrapper);

        // 检查文件是否存在
        if (file == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        // 检查用户是否有权限修改文件名
        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                ||  Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN))
                && !Objects.equals(file.getUserId(), user.getId())) {
            throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "无权限修改该文件");
        }

        String oldName = file.getName();
        String newName = updateNameRequest.getName();

        // 检查新文件名是否与旧文件名重复
        if (Objects.equals(oldName, newName)) {
            throw new CommonException(ErrorCode.PARAMS_ERROR, "与旧文件名重复");
        }

        file.setName(newName);
        boolean isUpdated = this.updateById(file);

        if (!isUpdated) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "文件名修改失败");
        }
        return "文件名修改成功" + oldName + "->" + newName;
    }

    @Override
    public String updateFile(UpdateFileRequest updateFileRequest, User user) {
        // 检查文件夹是否存在并属于当前用户
        // 1.查找对应文件
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getId, updateFileRequest.getId());
        File file = this.getOne(queryWrapper);

        // 检查文件是否存在
        if (file == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        if ((Objects.equals(user.getRole(), UserRoleConstant.USER)
                ||  Objects.equals(user.getRole(), UserRoleConstant.USER_ADMIN))
                && !Objects.equals(file.getUserId(), user.getId())) {
            throw new CommonException(ErrorCode.NOT_AUTH_ERROR, "无权限修改该文件");
        }

        String fileName = getFileNameFromPath(file.getPath());
        String path = fileUtils.getFolderPath(FileTypeEnum.DOCUMENT, file.getFolderId()) + fileName;

        // 2.用传入的content覆盖原本的内容
        try {
            Files.write(Paths.get(path), updateFileRequest.getContent().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "更新文件内容失败");
        }

        // 更新数据库中的文件信息（如果需要更新其他信息，如更新时间等）
        file.setUpdateUser(user.getId());
        this.updateById(file);

        return "文件更新成功，路径：" + path;
    }

    @Override
    public Map<String, String> readFileContent(Integer fileId, User user) {
        // 创建一个 Map 来返回内容和文件类型
        Map<String, String> result = new HashMap<>();
        // 1.获取文件路径
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getId, fileId);
        File file = this.getOne(queryWrapper);

        // 检查文件是否存在
        if (file == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        String fileName = getFileNameFromPath(file.getPath());
        String path = fileUtils.getFolderPath(FileTypeEnum.DOCUMENT, file.getFolderId()) + fileName;
        System.out.println(path);

        // 2.检查文件类型并读取文件内容
        String content;
        String fileExtension = fileUtils.getFileExtension(fileName);

        switch (fileExtension.toLowerCase()) {
            case "md":
                content = readFileContent(path);
                break;
            case "doc":
            case "docx":
                try {
                    content = fileUtils.convertDocToHtml(path);
                } catch (IOException | ParserConfigurationException e) {
                    throw new CommonException(ErrorCode.PARAMS_ERROR, "文件读取失败");
                }
                break;
            default:
                throw new CommonException(ErrorCode.PARAMS_ERROR, "不支持读取该类型文件");
        }

        // 3.返回文件内容和类型
        result.put("content", content);
        result.put("fileType", fileExtension.toLowerCase());
        return result;
    }

    @Override
    public List<FileDetailsVo> getFilesByFolderId(Integer folderId, boolean isLoggedIn) {
        List<File> files = fileMapper.getFilesByFolderId(folderId);
        List<FileDetailsVo> fileDetailsVos = new ArrayList<>();
        for (File file : files) {
            FileDetailsVo fileDetailsVo = new FileDetailsVo();
            fileDetailsVo.setId(file.getId());
            fileDetailsVo.setName(file.getName());
            fileDetailsVo.setPath(isLoggedIn ? file.getPath() : null);
            fileDetailsVo.setFileType(file.getFileType());
            fileDetailsVos.add(fileDetailsVo);
        }
        return fileDetailsVos;
    }

    public void addFilesToFolders(FolderDetailsVo folderDetailsVo, boolean isLoggedIn) {
        List<FileDetailsVo> files = this.getFilesByFolderId(folderDetailsVo.getId(), isLoggedIn);
        folderDetailsVo.setFiles(files);
        for (FolderDetailsVo subFolder : folderDetailsVo.getSubFolders()) {
            addFilesToFolders(subFolder, isLoggedIn);
        }
    }

    // 从文件路径中获取文件名
    private String getFileNameFromPath(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('\\');
        if (lastSlashIndex != -1 && lastSlashIndex < filePath.length() - 1) {
            return filePath.substring(lastSlashIndex);
        } else {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "文件路径格式不正确");
        }
    }

    // 读取文件内容
    private String readFileContent(String filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            return new String(fileBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommonException(ErrorCode.OPERATION_ERROR, "读取文件内容失败");
        }
    }

    // 处理Markdown文件
    private String dealMD(MultipartFile file, Integer folderId, Integer userId, String name,
                         FileTypeEnum fileType) throws IOException {
        byte[] fileBytes = file.getBytes();
        System.out.println("开始处理 Markdown 文件");

        // 提取图片路径
        List<String> imagePaths;
        imagePaths = fileImageUtils.extractImagePathsFromMD(fileBytes);
        System.out.println("提取的图片路径：" + imagePaths);

        File newFile = save(folderId, userId, name, fileType);

        // 获取文件ID
        Integer fileId = newFile.getId();

        // 保存图片路径到数据库并更新图片路径映射
        Map<String, String> imagePathMap = new HashMap<>();
        for (String imagePath : imagePaths) {
            byte[] imageBytes = fileImageUtils.readImage(imagePath);
            String serverPath = fileUtils.saveImage(imageBytes, fileId);
            imageUtils.saveImageDate(fileId, serverPath);
            imagePathMap.put(imagePath, serverPath);
        }

        // 更新Markdown内容并保存
        String updatedContent = fileImageUtils.replacePathsInMD(fileBytes, imagePathMap);
        String relativePath = fileUtils.saveMarkdownFile(updatedContent, fileType, folderId);
        newFile.setPath(relativePath);
        newFile.setUpdateUser(userId);
        this.updateById(newFile); // 更新文件路径
        return relativePath;
    }

    private File save(Integer folderId, Integer userId, String name,
                     FileTypeEnum fileType) {
        // 保存文件记录到数据库
        File newFile = new File();
        newFile.setFolderId(folderId);
        newFile.setUserId(userId);
        newFile.setName(name);
        newFile.setFileType(fileType.getType());
        newFile.setCreateUser(userId);
        newFile.setUpdateUser(userId);
        this.save(newFile);
        return newFile;
    }
}
