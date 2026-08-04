package com.yupe.siyun.service;

import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {
    private static final Set<String> ALLOWED_SUFFIXES = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            ".pdf", ".txt",
            ".mp4", ".mov", ".mkv", ".avi", ".flv", ".wmv", ".webm"
    );

    @Value("${upload.root-path}")
    private String basePath;
    @Value("${upload.url-prefix}")
    private String baseUrl;

    public String uploadFile(MultipartFile file, String subPath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "请选择要上传的文件");
        }
        String originName = file.getOriginalFilename();
        if (originName == null || !originName.contains(".")) {
            throw new MyException(ErrorType.FORMATE_ERROR,"文件名格式错误");
        }
        String suffix = originName.substring(originName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        if (!ALLOWED_SUFFIXES.contains(suffix)) {
            throw new MyException(ErrorType.FORMATE_ERROR,"文件格式错误");
        }
        String newFileName = UUID.randomUUID().toString() + suffix;

        String normalizedSubPath = normalizeSubPath(subPath);
        File targetDir = new File(basePath, normalizedSubPath);
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (!created && !targetDir.exists()) {
                throw new MyException(ErrorType.OPERATION_FAILED, "上传目录创建失败");
            }
        }

        File toFile = new File(targetDir, newFileName);
        file.transferTo(toFile);
        return buildPublicUrl(normalizedSubPath, newFileName);
    }

    public void deletePhysicalFile(String dbPath) {
        try {
            File targetFile = resolvePhysicalFile(dbPath);

            if (targetFile.exists() && targetFile.isFile()) {
                boolean deleted = targetFile.delete();
                if (deleted) {
                    System.out.println("成功删除物理文件: " + targetFile.getAbsolutePath());
                } else {
                    System.err.println("物理文件删除失败（可能有权限占用）: " + targetFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("尝试删除附件时发生异常: " + e.getMessage());
        }
    }

    private String normalizeSubPath(String subPath) {
        if (subPath == null || subPath.isBlank()) {
            return "";
        }
        String normalized = subPath.replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("..")) {
            throw new MyException(ErrorType.FORMATE_ERROR, "文件保存路径不合法");
        }
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    private String buildPublicUrl(String normalizedSubPath, String fileName) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String publicPath = normalizedSubPath;
        if (publicPath.startsWith("uploaded/")) {
            publicPath = publicPath.substring("uploaded/".length());
        }
        return normalizedBaseUrl + publicPath + fileName;
    }

    private File resolvePhysicalFile(String dbPath) {
        if (dbPath == null || dbPath.isBlank()) {
            return new File(basePath);
        }
        String normalized = dbPath.replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("uploaded/")) {
            normalized = normalized.substring("uploaded/".length());
            normalized = "uploaded/" + normalized;
        }
        return new File(basePath, normalized);
    }
}
