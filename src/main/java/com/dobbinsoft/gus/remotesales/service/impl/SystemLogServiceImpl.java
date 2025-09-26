package com.dobbinsoft.gus.remotesales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.GusRemotesalesApplication;
import com.dobbinsoft.gus.remotesales.aspect.log.LogRecord;
import com.dobbinsoft.gus.remotesales.data.po.OperationLogPO;
import com.dobbinsoft.gus.remotesales.mapper.OperationLogMapper;
import com.dobbinsoft.gus.remotesales.service.SystemLogService;
import com.dobbinsoft.gus.remotesales.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service

public class SystemLogServiceImpl implements SystemLogService {
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    ResourceLoader resourceLoader;

    private  static Set<String> LOG_MODEL_NAMES =null;
    private static void setLogModelNames(Set<String> logModelNames) {
        LOG_MODEL_NAMES=logModelNames;
    }
    public <T> Map<String, Map<String, Object>> getAllAnnotatedMethods(String classPath, Class<T> annotation) throws IOException {
        Map<String, Map<String, Object>> map = new HashMap<>();
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        Resource[] resources = resolver.getResources(classPath);
        for (Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            //逐个解析
            resolveClass(reader, map, annotation);
        }
        return map;
    }


    private <T> void resolveClass(MetadataReader reader, Map<String, Map<String, Object>> map, Class<T> annotation) {
        String annotationCanonicalName = annotation.getCanonicalName();
        //获取注解元数据
        AnnotationMetadata annotationMetadata = reader.getAnnotationMetadata();
        //获取类名
        String fullClassName = annotationMetadata.getClassName();
        String className = StringUtils.substring(fullClassName, fullClassName.lastIndexOf(".") + 1, fullClassName.length());
        //获取当前类中添加自定义注解的方法
        Set<MethodMetadata> annotatedMethods = annotationMetadata.getAnnotatedMethods(annotationCanonicalName);
        for (MethodMetadata annotatedMethod : annotatedMethods) {
            //获取方法名
            String methodName = annotatedMethod.getMethodName();
            //获取当前方法中要扫描注解的属性
            Map<String, Object> targetAttr = annotatedMethod.getAnnotationAttributes(annotationCanonicalName);
            map.put(className + "-" + methodName, targetAttr);
        }
    }

    @PostConstruct
    public void init() {

        try {
            Set<String> modelNames=new HashSet<>();
            Map<String, Map<String, Object>> data = getAllAnnotatedMethods("classpath*:" + StringUtils.replace(GusRemotesalesApplication.class.getPackageName(),".","/")+ "/**/*.class", LogRecord.class);
            data.forEach((key, value) -> modelNames.add(value.get("modelName").toString()));
            setLogModelNames(Collections.unmodifiableSet(modelNames));
        } catch (IOException e) {
            log.error("init logRecord model Name error {}", e.getMessage());
        }
    }

    @Override
    public List<String> getAllLogModelName() {
        return new ArrayList<>(LOG_MODEL_NAMES);
    }

    @Override
    public PageResult<OperationLogPO> pageList(Integer pageNum, Integer pageSize, String modelName, ZonedDateTime startTime, ZonedDateTime endTime, String keyword) {
        Page<OperationLogPO> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<OperationLogPO> queryWrapper = new LambdaQueryWrapper<OperationLogPO>()
                .eq(StringUtils.isNotBlank(modelName), OperationLogPO::getModelName, modelName)
                .ge(Objects.nonNull(startTime), OperationLogPO::getOperateTime, startTime)
                .le(Objects.nonNull(endTime), OperationLogPO::getOperateTime, endTime)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                    .like(OperationLogPO::getUserName, keyword)
                    .or()
                    .like(OperationLogPO::getAction, keyword)
                    .or()
                    .like(OperationLogPO::getComment, keyword)
                    .or()
                    .like(OperationLogPO::getIpAddress, keyword))
                .eq(OperationLogPO::getDeleted, Boolean.FALSE);

        queryWrapper.orderByDesc(OperationLogPO::getOperateTime);
        Page<OperationLogPO> resultPage = operationLogMapper.selectPage(page, queryWrapper);
        return PageResult.<OperationLogPO>builder()
                .totalCount(resultPage.getTotal())
                .totalPages(resultPage.getPages())
                .pageNumber(pageNum)
                .pageSize(pageSize)
                .hasMore(resultPage.getCurrent() < resultPage.getPages())
                .data(resultPage.getRecords())
                .build();
    }

    @Override
    public void exportExcel(HttpServletResponse response, String modelName, ZonedDateTime startTime, ZonedDateTime endTime, String keyword) throws IOException {
        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("operation_log".concat(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(ZonedDateTime.now())));
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"账号", "用户名", "模块", "详情", "日期"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 分页查询并写入数据
            int pageSize = 1000; // 每页1000条数据
            int pageNum = 1;
            int rowNum = 1;
            boolean hasMore = true;

            while (hasMore) {
                PageResult<OperationLogPO> pageResult = pageList(pageNum, pageSize, modelName, startTime, endTime, keyword);
                List<OperationLogPO> logList = pageResult.getData();

                if (logList == null || logList.isEmpty()) {
                    hasMore = false;
                    continue;
                }

                // 写入数据
                for (OperationLogPO log : logList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(log.getWwid());
                    row.createCell(1).setCellValue(log.getUserName());
                    row.createCell(2).setCellValue(log.getModelName());
                    row.createCell(3).setCellValue(log.getComment());
                    row.createCell(4).setCellValue(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(log.getOperateTime()));
                }

                // 判断是否还有更多数据
                hasMore = logList.size() == pageSize;
                pageNum++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 25*256);
            }
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("operation_log" + DateUtils.localDateTimeToString(LocalDateTime.now(), "yyyy_MM_dd_HH_mm_ss") , StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 写入响应流
            workbook.write(response.getOutputStream());
        }
    }
}