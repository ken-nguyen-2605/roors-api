package com.josephken.roors.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    private String timestamp;
    private String level;
    private String category;
    private String message;
    private String thread;
    private String logger;
}
