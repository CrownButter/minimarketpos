package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.response.ReportDTO;
import com.pos.minimarketpos.repository.ExpenseRepository;
import com.pos.minimarketpos.repository.SaleRepository;
import com.pos.minimarketpos.repository.SaleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public ReportDTO getDashboardReport() {
        // Get today's sales
        BigDecimal todaySales = saleRepository.getTodayTotal();
        if (todaySales == null) todaySales = BigDecimal.ZERO;

        // Get today's profit
        BigDecimal todayProfit = saleRepository.getTodayProfit();
        if (todayProfit == null) todayProfit = BigDecimal.ZERO;

        // Calculate today's cost
        BigDecimal todayCost = todaySales.subtract(todayProfit);

        // Get today's orders count
        Integer todayOrders = saleRepository.findByDateRange(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        ).size();

        // Get monthly sales for current year
        Map<String, BigDecimal> monthlySales = getMonthlySales(LocalDate.now().getYear());

        // Get monthly expenses for current year
        Map<String, BigDecimal> monthlyExpenses = getMonthlyExpenses(LocalDate.now().getYear());

        // Get top products
        List<ReportDTO.ProductSales> topProducts = getTopProducts(LocalDate.now().getYear());

        return ReportDTO.builder()
                .todaySales(todaySales)
                .todayProfit(todayProfit)
                .todayCost(todayCost)
                .todayOrders(todayOrders)
                .monthlySales(monthlySales)
                .monthlyExpenses(monthlyExpenses)
                .topProducts(topProducts)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlySales(int year) {
        Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.atEndOfMonth();

            BigDecimal total = saleRepository.findByDateRange(
                            start.atStartOfDay(),
                            end.plusDays(1).atStartOfDay()
                    ).stream()
                    .map(sale -> sale.getTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlySales.put(yearMonth.getMonth().toString(), total);
        }

        return monthlySales;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyExpenses(int year) {
        Map<String, BigDecimal> monthlyExpenses = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            BigDecimal total = expenseRepository.getMonthlyTotal(year, month);
            if (total == null) total = BigDecimal.ZERO;

            YearMonth yearMonth = YearMonth.of(year, month);
            monthlyExpenses.put(yearMonth.getMonth().toString(), total);
        }

        return monthlyExpenses;
    }

    @Transactional(readOnly = true)
    public List<ReportDTO.ProductSales> getTopProducts(int year) {
        List<Object[]> results = saleItemRepository.findTop5Products(year);

        return results.stream()
                .limit(5)
                .map(result -> {
                    String productName = (String) result[0];
                    Long productId = ((Number) result[1]).longValue();
                    Integer totalQuantity = ((Number) result[2]).intValue();

                    // Calculate total amount for this product
                    BigDecimal totalAmount = saleItemRepository.findByProductId(productId)
                            .stream()
                            .filter(item -> item.getDate().getYear() == year)
                            .map(item -> item.getSubtotal())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ReportDTO.ProductSales.builder()
                            .productId(productId)
                            .productName(productName)
                            .totalQuantity(totalQuantity)
                            .totalAmount(totalAmount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportDTO getSalesReportByDateRange(LocalDate startDate, LocalDate endDate) {
        var sales = saleRepository.findByDateRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        BigDecimal totalSales = sales.stream()
                .map(sale -> sale.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = sales.stream()
                .map(sale -> sale.getCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = totalSales.subtract(totalCost);

        Integer totalOrders = sales.size();

        return ReportDTO.builder()
                .todaySales(totalSales)
                .todayProfit(totalProfit)
                .todayCost(totalCost)
                .todayOrders(totalOrders)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProductPerformanceReport(Long productId, int year) {
        Map<String, Object> report = new HashMap<>();

        var saleItems = saleItemRepository.findByProductId(productId)
                .stream()
                .filter(item -> item.getDate().getYear() == year)
                .collect(Collectors.toList());

        Integer totalQuantitySold = saleItems.stream()
                .mapToInt(item -> item.getQt())
                .sum();

        BigDecimal totalRevenue = saleItems.stream()
                .map(item -> item.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("productId", productId);
        report.put("year", year);
        report.put("totalQuantitySold", totalQuantitySold);
        report.put("totalRevenue", totalRevenue);

        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCashFlowReport(int year, int month) {
        Map<String, BigDecimal> cashFlow = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        // Get sales income
        BigDecimal income = saleRepository.findByDateRange(
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay()
                ).stream()
                .map(sale -> sale.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get expenses
        BigDecimal expenses = expenseRepository.findByDateRange(start, end)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netCashFlow = income.subtract(expenses);

        cashFlow.put("income", income);
        cashFlow.put("expenses", expenses);
        cashFlow.put("netCashFlow", netCashFlow);

        return cashFlow;
    }
}