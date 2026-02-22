package edu.zsc.ai.controller.db;

import cn.dev33.satoken.stp.StpUtil;
import edu.zsc.ai.domain.model.dto.response.base.ApiResponse;
import edu.zsc.ai.domain.service.db.DatabaseExportService;
import edu.zsc.ai.domain.service.db.DatabaseImportService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseExportImportController {

    private final DatabaseExportService databaseExportService;
    private final DatabaseImportService databaseImportService;

    @GetMapping("/export")
    public ApiResponse<String> exportDatabaseDdl(
            @RequestParam @NotNull(message = "connectionId is required") Long connectionId,
            @RequestParam @NotBlank(message = "databaseName is required") String databaseName) {
        log.info("Exporting database DDL: connectionId={}, databaseName={}", connectionId, databaseName);
        long userId = StpUtil.getLoginIdAsLong();
        String ddl = databaseExportService.exportDatabaseDdl(connectionId, databaseName, userId);
        return ApiResponse.success(ddl);
    }

    @GetMapping("/export-tables")
    public ApiResponse<List<String>> exportAllTableDdls(
            @RequestParam @NotNull(message = "connectionId is required") Long connectionId,
            @RequestParam @NotBlank(message = "databaseName is required") String databaseName) {
        log.info("Exporting all table DDLs: connectionId={}, databaseName={}", connectionId, databaseName);
        long userId = StpUtil.getLoginIdAsLong();
        List<String> ddls = databaseExportService.exportAllTableDdls(connectionId, databaseName, userId);
        return ApiResponse.success(ddls);
    }

    @PostMapping("/import")
    public ApiResponse<Void> importDatabase(
            @RequestParam @NotNull(message = "connectionId is required") Long connectionId,
            @RequestParam @NotBlank(message = "sqlScript is required") String sqlScript) {
        log.info("Importing database: connectionId={}", connectionId);
        long userId = StpUtil.getLoginIdAsLong();
        databaseImportService.executeSqlScript(connectionId, sqlScript, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/import/file")
    public ApiResponse<Void> importDatabaseFromFile(
            @RequestParam @NotNull(message = "connectionId is required") Long connectionId,
            @RequestParam @NotNull(message = "file is required") MultipartFile file) {
        log.info("Importing database from file: connectionId={}, fileName={}", connectionId, file.getOriginalFilename());
        try {
            String sqlScript = new String(file.getBytes(), StandardCharsets.UTF_8);
            long userId = StpUtil.getLoginIdAsLong();
            databaseImportService.executeSqlScript(connectionId, sqlScript, userId);
            return ApiResponse.success(null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }
}
