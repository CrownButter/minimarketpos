package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.response.ReportDTO;
import com.pos.minimarketpos.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ReportDTO> getDashboardReport() {
        return ResponseEntity.ok(reportService.getDashboardReport());
    }

    @GetMapping("/sales/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlySales(
            @RequestParam(defaultValue = "2024") int year) {
        return ResponseEntity.ok(reportService.getMonthlySales(year));
    }

    @GetMapping("/expenses/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyExpenses(
            @RequestParam(defaultValue = "2024") int year) {
        return ResponseEntity.ok(reportService.getMonthlyExpenses(year));
    }

    @GetMapping("/products/top")
    public ResponseEntity<List<ReportDTO.ProductSales>> getTopProducts(
            @RequestParam(defaultValue = "2024") int year) {
        return ResponseEntity.ok(reportService.getTopProducts(year));
    }

    @GetMapping("/sales/date-range")
    public ResponseEntity<ReportDTO> getSalesReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getSalesReportByDateRange(startDate, endDate));
    }

    @GetMapping("/product/{productId}/performance")
    public ResponseEntity<Map<String, Object>> getProductPerformanceReport(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "2024") int year) {
        return ResponseEntity.ok(reportService.getProductPerformanceReport(productId, year));
    }

    @GetMapping("/cashflow")
    public ResponseEntity<Map<String, BigDecimal>> getCashFlowReport(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reportService.getCashFlowReport(year, month));
    }
}