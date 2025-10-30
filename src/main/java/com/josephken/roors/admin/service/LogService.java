package com.josephken.roors.admin.service;

import com.josephken.roors.admin.dto.LogEntry;
import com.josephken.roors.admin.dto.LogResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LogService {

    @Value("${logging.file.name:logs/roors-api.log}")
    private String logFilePath;

    // Pattern to parse log lines: timestamp [thread] level logger - message
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[([^\\]]+)\\]\\s+(\\w+)\\s+([^\\s]+)\\s+-\\s+(.+)$"
    );

    // Pattern to extract category from message
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^\\[(\\w+)\\]\\s+(.*)$");

    /**
     * Read logs with optional filters
     */
    public LogResponse getLogs(String category, String level, Integer limit, Integer page) {
        List<LogEntry> allLogs = readLogFile();

        // Apply filters
        List<LogEntry> filteredLogs = allLogs.stream()
                .filter(log -> category == null || category.isEmpty() || 
                        (log.getCategory() != null && log.getCategory().equalsIgnoreCase(category)))
                .filter(log -> level == null || level.isEmpty() || 
                        log.getLevel().equalsIgnoreCase(level))
                .collect(Collectors.toList());

        // Apply pagination
        int pageSize = limit != null ? limit : 100;
        int currentPage = page != null ? page : 1;
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredLogs.size());

        List<LogEntry> paginatedLogs = startIndex < filteredLogs.size() 
                ? filteredLogs.subList(startIndex, endIndex)
                : new ArrayList<>();

        return new LogResponse(paginatedLogs, filteredLogs.size(), pageSize, currentPage);
    }

    /**
     * Get available log categories
     */
    public List<String> getCategories() {
        List<LogEntry> allLogs = readLogFile();
        return allLogs.stream()
                .map(LogEntry::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Read and parse the log file
     */
    private List<LogEntry> readLogFile() {
        List<LogEntry> logs = new ArrayList<>();
        Path path = Paths.get(logFilePath);

        if (!Files.exists(path)) {
            log.warn("Log file not found: {}", logFilePath);
            return logs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogEntry entry = parseLogLine(line);
                if (entry != null) {
                    logs.add(entry);
                }
            }
        } catch (IOException e) {
            log.error("Error reading log file: {}", e.getMessage());
        }

        // Reverse to show newest logs first
        List<LogEntry> reversedLogs = new ArrayList<>();
        for (int i = logs.size() - 1; i >= 0; i--) {
            reversedLogs.add(logs.get(i));
        }

        return reversedLogs;
    }

    /**
     * Parse a single log line
     */
    private LogEntry parseLogLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (matcher.matches()) {
            String timestamp = matcher.group(1);
            String thread = matcher.group(2);
            String level = matcher.group(3);
            String logger = matcher.group(4);
            String message = matcher.group(5);

            // Extract category from message if present
            String category = null;
            Matcher categoryMatcher = CATEGORY_PATTERN.matcher(message);
            if (categoryMatcher.matches()) {
                category = categoryMatcher.group(1);
                message = categoryMatcher.group(2);
            }

            return new LogEntry(timestamp, level, category, message, thread, logger);
        }
        return null;
    }
}
