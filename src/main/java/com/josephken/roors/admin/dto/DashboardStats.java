package com.josephken.roors.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStats {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long totalMenuItems;
    private long totalUsers;
    private List<OrderStatsByDate> revenueOverTime;
    private List<TopSellingItem> topSellingItems;
    private Map<String, Long> orderStatusDistribution;
    private List<CategorySales> categorySales;
}
