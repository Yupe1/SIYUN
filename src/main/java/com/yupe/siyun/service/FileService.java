package com.yupe.siyun.service;




import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {
    @Value("${upload.path}")
    private String basePath;
    @Value("${upload.url}")
    private String baseUrl;
    public String uploadFile(MultipartFile file, String subPath) throws IOException {
        String originName = file.getOriginalFilename();
        String suffix = originName.substring(originName.lastIndexOf("."));
        if(!(suffix.equalsIgnoreCase(".jpg")
                || suffix.equalsIgnoreCase(".jpeg")
                || suffix.equalsIgnoreCase("png")
                || suffix.equalsIgnoreCase(".pdf")
                || suffix.equalsIgnoreCase(".txt"))) {
            throw new MyException(ErrorType.FORMATE_ERROR,"文件格式错误");
        }
        String newFileName = UUID.randomUUID().toString() + suffix;
//==========================================================================
        // 3. 构建安全的绝对路径 (排雷2：用 File 构造器代替 String 的 + 拼接)
        // 假设 basePath 是 /opt/homebrew/var/www
        // 假设 subPath 是 uploaded/book/
        File targetDir = new File(basePath, subPath);

        // 4. 排雷1：检查并创建多级父目录 (非常关键！)
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // 5. 将文件保存到目标位置
        File toFile = new File(targetDir, newFileName);
        file.transferTo(toFile);

        // 6. 生成返回给前端的 URL
        // 你的 indexOf 方法很巧妙，但需要确保 newFilePath.getAbsolutePath() 里一定包含 baseUrl
        // 更安全的做法是：baseUrl + subPath + newFileName (这里简单处理斜杠问题)
        String absolutePath = toFile.getAbsolutePath();
        int urlIndex = absolutePath.indexOf(baseUrl);
        if (urlIndex != -1) {
            return absolutePath.substring(urlIndex);
        } else {
            // 降级容错方案，以防 indexOf 匹配不到
            return baseUrl + subPath + newFileName;
        }
//==========================================================================
//        String newFilePath = Path + path + newFileName;
//        File toFile = new File(newFilePath);
//        file.transferTo(toFile);
//        String url = newFilePath.substring(newFilePath.indexOf(Url));
//        return url;
    }
    public void deletePhysicalFile(String dbPath) {
        try {
            // 将基础路径和数据库路径拼接。
            // 因为你的 basePath 是 /opt/homebrew/var/www，dbPath 是 /uploaded/book/xxx.pdf
            // 拼起来就是完整的 Mac 本地绝对路径
            File targetFile = new File(basePath, dbPath);

            // 判断文件是否存在且是一个普通文件
            if (targetFile.exists() && targetFile.isFile()) {
                boolean deleted = targetFile.delete();
                if (deleted) {
                    System.out.println("成功删除物理文件: " + targetFile.getAbsolutePath());
                } else {
                    System.err.println("物理文件删除失败（可能有权限占用）: " + targetFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            // 💡 关键点：删文件报错不应该导致整个事务回滚（书已经删了，留个孤儿文件总比删不掉书强）
            System.err.println("尝试删除附件时发生异常: " + e.getMessage());
        }
    }
}
