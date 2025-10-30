package com.josephken.roors.admin.controller;

import com.josephken.roors.admin.dto.LogResponse;
import com.josephken.roors.admin.service.LogService;
import com.josephken.roors.util.LogCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/logs")
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * Get logs with optional filters
     * GET /admin/logs?category=USER&level=INFO&limit=50&page=1
     */
    @GetMapping
    public ResponseEntity<LogResponse> getLogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        log.info(LogCategory.admin("Viewing logs - category: {}, level: {}, limit: {}, page: {}"), 
                category, level, limit, page);
        
        LogResponse response = logService.getLogs(category, level, limit, page);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available log categories
     * GET /admin/logs/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        log.info(LogCategory.admin("Requesting log categories"));
        
        List<String> categories = logService.getCategories();
        return ResponseEntity.ok(categories);
    }
}
