package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.response.CashflowDTO;
import com.pos.minimarketpos.service.CashflowService;
import com.pos.minimarketpos.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/cashflows/export")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class CashflowExportController {

    private final CashflowService cashflowService;

    /**
     * Export all cashflows to PDF
     * GET /api/cashflows/export/pdf
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportAllCashflowsToPdf() {
        List<CashflowDTO> cashflows = cashflowService.getAllCashflows();
        String title = "Cashflow Report - All Records";

        return generatePdfResponse(cashflows, title, "cashflow_report_all.pdf");
    }

    /**
     * Export cashflows by store to PDF
     * GET /api/cashflows/export/pdf/store/{storeId}
     */
    @GetMapping("/pdf/store/{storeId}")
    public ResponseEntity<byte[]> exportCashflowsByStoreToPdf(@PathVariable Long storeId) {
        List<CashflowDTO> cashflows = cashflowService.getCashflowsByStore(storeId);

        String storeName = !cashflows.isEmpty() && cashflows.get(0).getStoreName() != null
                ? cashflows.get(0).getStoreName()
                : "Store ID " + storeId;
        String title = "Cashflow Report - " + storeName;

        return generatePdfResponse(cashflows, title,
                "cashflow_report_store_" + storeId + ".pdf");
    }

    /**
     * Export cashflows by category to PDF
     * GET /api/cashflows/export/pdf/category/{categoryId}
     */
    @GetMapping("/pdf/category/{categoryId}")
    public ResponseEntity<byte[]> exportCashflowsByCategoryToPdf(@PathVariable Long categoryId) {
        List<CashflowDTO> cashflows = cashflowService.getCashflowsByCategory(categoryId);

        String categoryName = !cashflows.isEmpty() && cashflows.get(0).getCategoryName() != null
                ? cashflows.get(0).getCategoryName()
                : "Category ID " + categoryId;
        String title = "Cashflow Report - " + categoryName;

        return generatePdfResponse(cashflows, title,
                "cashflow_report_category_" + categoryId + ".pdf");
    }

    /**
     * Export cashflows by date range to PDF
     * GET /api/cashflows/export/pdf/date-range?start=2024-01-01&end=2024-12-31
     */
    @GetMapping("/pdf/date-range")
    public ResponseEntity<byte[]> exportCashflowsByDateRangeToPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<CashflowDTO> cashflows = cashflowService.getCashflowsByDateRange(start, end);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String title = String.format("Cashflow Report - %s to %s",
                start.format(formatter), end.format(formatter));

        String filename = String.format("cashflow_report_%s_to_%s.pdf",
                start.toString(), end.toString());

        return generatePdfResponse(cashflows, title, filename);
    }

    /**
     * Export monthly cashflows to PDF
     * GET /api/cashflows/export/pdf/monthly?year=2024&month=12
     */
    @GetMapping("/pdf/monthly")
    public ResponseEntity<byte[]> exportMonthlyCashflowsToPdf(
            @RequestParam int year,
            @RequestParam int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<CashflowDTO> cashflows = cashflowService.getCashflowsByDateRange(start, end);

        String monthName = start.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        String title = "Cashflow Report - " + monthName;

        String filename = String.format("cashflow_report_%d_%02d.pdf", year, month);

        return generatePdfResponse(cashflows, title, filename);
    }

    /**
     * Export filtered cashflows with multiple criteria to PDF
     * POST /api/cashflows/export/pdf/filtered
     * Body: { "storeId": 1, "categoryId": 2, "start": "2024-01-01", "end": "2024-12-31", "lunas": true }
     */
    @PostMapping("/pdf/filtered")
    public ResponseEntity<byte[]> exportFilteredCashflowsToPdf(
            @RequestBody CashflowFilterRequest filterRequest) {

        List<CashflowDTO> cashflows = cashflowService.getFilteredCashflows(filterRequest);

        String title = "Cashflow Report - Custom Filter";
        String filename = "cashflow_report_filtered_" + System.currentTimeMillis() + ".pdf";

        return generatePdfResponse(cashflows, title, filename);
    }

    /**
     * Helper method to generate PDF response
     */
    private ResponseEntity<byte[]> generatePdfResponse(
            List<CashflowDTO> cashflows, String title, String filename) {

        try {
            byte[] pdfBytes = PdfGenerator.generateCashflowPdfBytes(cashflows, title);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DTO for filtered export request
     */
    @lombok.Data
    public static class CashflowFilterRequest {
        private Long storeId;
        private Long categoryId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate start;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate end;

        private Boolean lunas;
    }
}