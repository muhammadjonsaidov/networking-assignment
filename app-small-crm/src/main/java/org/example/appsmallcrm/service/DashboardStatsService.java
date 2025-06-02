package org.example.appsmallcrm.service;

import lombok.RequiredArgsConstructor;
import org.example.appsmallcrm.entity.Activity;
import org.example.appsmallcrm.entity.Customer;
import org.example.appsmallcrm.entity.DashboardStats;
import org.example.appsmallcrm.repo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Most dashboard methods are read-only
public class DashboardStatsService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ActivityRepository activityRepository;
    private final OrderRepository orderRepository; // For total order count etc.


    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        LocalDate today = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(today);
        LocalDate currentMonthStart = currentYearMonth.atDay(1);
        // For "current month" stats, using 'today' as end date gives up-to-the-minute,
        // while 'atEndOfMonth' gives stats for the full month IF it's already passed.
        // Let's use 'today' for current month to reflect current progress.


        YearMonth lastYearMonth = currentYearMonth.minusMonths(1);
        LocalDate lastMonthStart = lastYearMonth.atDay(1);
        LocalDate lastMonthEnd = lastYearMonth.atEndOfMonth();

        Double revenueCurrentMonth = saleRepository.sumRevenueBetweenDates(currentMonthStart, today);
        Long soldItemsCountCurrentMonth = saleRepository.countSalesBetweenDates(currentMonthStart, today);
        Double avgSalesQtyCurrentMonth = saleRepository.avgSalesQuantityBetweenDates(currentMonthStart, today);

        Double revenueLastMonth = saleRepository.sumRevenueBetweenDates(lastMonthStart, lastMonthEnd);
        Long soldItemsCountLastMonth = saleRepository.countSalesBetweenDates(lastMonthStart, lastMonthEnd);
        Double avgSalesQtyLastMonth = saleRepository.avgSalesQuantityBetweenDates(lastMonthStart, lastMonthEnd);


        stats.setTotalProduct((int) productRepository.count()); // Total unique products available
        stats.setProductRevenue(revenueCurrentMonth != null ? revenueCurrentMonth : 0.0);
        stats.setProductSold(soldItemsCountCurrentMonth != null ? soldItemsCountCurrentMonth.intValue() : 0); // Number of sales transactions
        stats.setAvgMonthlySales(avgSalesQtyCurrentMonth != null ? avgSalesQtyCurrentMonth : 0.0); // This is avg quantity PER SALE for current month

        stats.setRevenueChange(calcPercentageChange(revenueLastMonth, revenueCurrentMonth));
        stats.setSoldChange(calcPercentageChange(
                soldItemsCountLastMonth != null ? soldItemsCountLastMonth.doubleValue() : null,
                soldItemsCountCurrentMonth != null ? soldItemsCountCurrentMonth.doubleValue() : null
        ));
        stats.setAvgSalesChange(calcPercentageChange(avgSalesQtyLastMonth, avgSalesQtyCurrentMonth)); // Change in avg quantity per sale

        return stats;
    }

    private double calcPercentageChange(Double previous, Double current) {
        if (previous == null || previous == 0.0) {
            return (current != null && current != 0.0) ? 100.0 : 0.0;
        }
        if (current == null || current == 0.0) { // Corrected handling for current is null or zero
            return -100.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    public List<Map<String, Object>> getBarData() { // Monthly sales for current year
        int currentYear = LocalDate.now().getYear();
        List<Object[]> monthlySales = saleRepository.findMonthlySalesForYear(currentYear);
        return monthlySales.stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", getMonthName((Integer) data[0])); // data[0] is month number
                    map.put("total", data[1] != null ? (Double) data[1] : 0.0); // data[1] is totalRevenue
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getLineData() { // Daily sales for last 30 days
        LocalDate startDate = LocalDate.now().minusDays(30);
        List<Object[]> dailySales = saleRepository.findDailySalesFrom(startDate);
        return dailySales.stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    // Handle java.sql.Date if returned by native query from JPA
                    // If data[0] is already LocalDate, no conversion needed.
                    LocalDate saleDate;
                    if (data[0] instanceof java.sql.Date) {
                        saleDate = ((java.sql.Date) data[0]).toLocalDate();
                    } else if (data[0] instanceof LocalDate) {
                        saleDate = (LocalDate) data[0];
                    } else {
                        throw new IllegalStateException("Unexpected date type from query: " + data[0].getClass().getName());
                    }
                    map.put("date", saleDate.toString());
                    map.put("total", data[1] != null ? (Double) data[1] : 0.0);
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPieData() { // Revenue by product
        List<Object[]> salesByProduct = saleRepository.findTotalRevenueByProductName();
        return salesByProduct.stream()
                .map(data -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("product", (String) data[0]); // productName
                    map.put("total", data[1] != null ? (Double) data[1] : 0.0); // totalRevenue
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Customer> getRecentCustomers(int count) { // Get N most recent customers
        return customerRepository.findAll(PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    public List<Activity> getRecentActivities(int count) { // Get N most recent activities
        return activityRepository.findAll(PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent();
    }

    private String getMonthName(int month) {
        return Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
}