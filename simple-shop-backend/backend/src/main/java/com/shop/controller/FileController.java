package com.shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // 允许的图片格式
    private static final Set<String> ALLOWED_IMAGE_FORMATS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    ));

    // 最大图片大小（5MB）
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 最多上传图片数量
    private static final int MAX_FILE_COUNT = 10;

    // 文件存储路径（从配置文件读取）
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 上传图片接口
     * 支持单次上传多张图片（最多10张）
     * 限制图片大小和格式
     */
    @PostMapping("/upload/images")
    public ResponseEntity<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        // 验证文件数量
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "请选择要上传的图片"));
        }

        if (files.length > MAX_FILE_COUNT) {
            return ResponseEntity.badRequest().body(Map.of("error", "最多只能上传" + MAX_FILE_COUNT + "张图片"));
        }

        List<String> imageUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 创建上传目录（如果不存在）
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // 处理每个文件
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];

            // 跳过空文件
            if (file.isEmpty()) {
                errors.add("第" + (i + 1) + "个文件为空");
                continue;
            }

            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                errors.add("第" + (i + 1) + "个文件大小超过限制（最大5MB）");
                continue;
            }

            // 验证文件格式
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.contains(".")) {
                errors.add("第" + (i + 1) + "个文件格式无效");
                continue;
            }

            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_IMAGE_FORMATS.contains(fileExtension)) {
                errors.add("第" + (i + 1) + "个文件格式不支持，仅支持：" + String.join(", ", ALLOWED_IMAGE_FORMATS));
                continue;
            }

            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            Path targetLocation = Paths.get(uploadDir + File.separator + uniqueFileName);

            try {
                // 保存文件
                Files.copy(file.getInputStream(), targetLocation);
                // 生成访问URL（这里使用相对路径，实际部署时可能需要配置绝对URL）
                String imageUrl = "/api/files/images/" + uniqueFileName;
                imageUrls.add(imageUrl);
            } catch (IOException e) {
                errors.add("第" + (i + 1) + "个文件上传失败：" + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("imageUrls", imageUrls);
        
        if (!errors.isEmpty()) {
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取图片资源
     * 这个方法需要配合Spring的静态资源配置使用
     * 或者在WebConfig中添加相应的资源映射
     */
    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        try {
            Path imagePath = Paths.get(uploadDir + File.separator + fileName);
            if (Files.exists(imagePath)) {
                String contentType = Files.probeContentType(imagePath);
                byte[] imageData = Files.readAllBytes(imagePath);
                return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(imageData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取富媒体编辑器配置信息
     * 为前端富媒体编辑器提供必要的配置参数
     */
    @GetMapping("/editor/config")
    public ResponseEntity<?> getEditorConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("uploadUrl", "/api/files/upload/images");
        config.put("allowedImageFormats", ALLOWED_IMAGE_FORMATS);
        config.put("maxFileSize", MAX_FILE_SIZE);
        config.put("maxFileCount", MAX_FILE_COUNT);
        return ResponseEntity.ok(config);
    }
}