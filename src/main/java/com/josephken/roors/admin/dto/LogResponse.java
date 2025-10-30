package com.josephken.roors.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogResponse {
    private List<LogEntry> logs;
    private int totalCount;
    private int pageSize;
    private int currentPage;
}
