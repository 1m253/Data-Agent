package edu.zsc.ai.controller.db;

import edu.zsc.ai.domain.model.dto.response.base.ApiResponse;
import edu.zsc.ai.domain.model.dto.response.db.DbTypeOption;
import edu.zsc.ai.plugin.enums.DbType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Database type controller.
 * Exposes supported database types for frontend (e.g. connection form dropdown).
 *
 * @author Data-Agent
 * @since 0.0.1
 */
@Slf4j
@RestController
@RequestMapping("/api/db-types")
@RequiredArgsConstructor
public class DbTypeController {

    /**
     * List all supported database types.
     * Returns code and displayName (no enum in API).
     *
     * @return list of supported database type options
     */
    @GetMapping
    public ApiResponse<List<DbTypeOption>> listSupportedDbTypes() {
        log.debug("Listing supported database types");
        List<DbTypeOption> options = Arrays.stream(DbType.values())
                .map(t -> DbTypeOption.builder()
                        .code(t.getCode())
                        .displayName(t.getDisplayName())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(options);
    }
}
