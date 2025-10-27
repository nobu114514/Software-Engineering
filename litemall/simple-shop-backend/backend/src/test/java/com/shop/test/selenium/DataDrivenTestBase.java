package com.shop.test.selenium;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据驱动测试基类，提供CSV数据处理功能
 */
public abstract class DataDrivenTestBase extends TestBase {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 从CSV文件加载测试数据
     * @param csvPath CSV文件路径
     * @return 测试数据流
     */
    protected Stream<Map<String, String>> loadTestData(String csvPath) {
        logger.info("=== 加载测试数据开始 ===");
        logger.info("CSV文件路径: {}", csvPath);
        List<Map<String, String>> dataList = new ArrayList<>();
        
        try {
            // 读取CSV文件内容
            String content = new String(Files.readAllBytes(Paths.get(csvPath)), StandardCharsets.UTF_8);
            
            // 处理BOM字符
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), 
                    StandardCharsets.UTF_8))) {
                
                String headerLine = reader.readLine();
                logger.info("读取到的表头: {}", headerLine);
                
                if (headerLine == null) {
                    logger.error("错误：CSV文件为空");
                    return Stream.empty();
                }
                
                // 自动检测分隔符
                String[] headers = detectDelimiter(headerLine);
                
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("读取到的行内容: {}", line);
                    String[] values = detectDelimiter(line);
                    logger.info("解析后的字段数: {}", values.length);
                    
                    if (values.length >= headers.length) {
                        Map<String, String> data = new HashMap<>();
                        for (int i = 0; i < headers.length; i++) {
                            data.put(headers[i], i < values.length ? values[i] : "");
                        }
                        dataList.add(data);
                        logger.info("成功添加测试数据: {}", data.get("用例 ID"));
                    } else {
                        logger.warn("跳过无效行（字段数不足）: {}", line);
                    }
                }
                
                logger.info("=== 加载完成，共{}条有效测试用例 ===", dataList.size());
            }
        } catch (Exception e) {
            logger.error("错误：读取CSV文件失败 - {}", e.getMessage());
            e.printStackTrace();
        }
        
        return dataList.stream();
    }
    
    /**
     * 自动检测CSV文件的分隔符
     * @param line CSV文件中的一行
     * @return 解析后的字段数组
     */
    protected String[] detectDelimiter(String line) {
        if (line.contains("\t")) {
            return line.split("\\t", -1);
        } else {
            return line.split(",", -1);
        }
    }
}