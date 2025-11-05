package com.josephken.roors.admin.service;

import com.josephken.roors.admin.dto.DashboardStats;
import com.josephken.roors.admin.dto.OrderStatsByDate;
import com.josephken.roors.admin.dto.TopSellingItem;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.menu.repository.MenuItemRepository;
import com.josephken.roors.order.entity.OrderStatus;
import com.josephken.roors.order.repository.OrderItemRepository;
import com.josephken.roors.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardStats getDashboardStatistics(int days) {
        LocalDateTime since = LocalDate.now().minusDays(days).atStartOfDay();

        // Use count and query methods instead of loading all data into memory
        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);
        long totalMenuItems = menuItemRepository.count();
        long totalUsers = userRepository.count();

        List<OrderStatsByDate> revenueOverTime = orderRepository.findOrderStatsByDate(since);
        List<TopSellingItem> topSellingItems = orderItemRepository.findTopSellingItems()
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Long> orderStatusDistribution = orderRepository.countOrdersByStatus()
                .stream()
                .collect(Collectors.toMap(
                        obj -> ((OrderStatus) obj[0]).name(),
                        obj -> (Long) obj[1]
                ));

        return DashboardStats.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalMenuItems(totalMenuItems)
                .totalUsers(totalUsers)
                .revenueOverTime(revenueOverTime)
                .topSellingItems(topSellingItems)
                .orderStatusDistribution(orderStatusDistribution)
                .build();
    }
}
