package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.response.CashflowDTO;
import com.pos.minimarketpos.model.Cashflow;
import com.pos.minimarketpos.model.CategoryCashflow;
import com.pos.minimarketpos.model.Store;
import com.pos.minimarketpos.model.User;
import com.pos.minimarketpos.repository.CategoryCashflowRepository;
import com.pos.minimarketpos.repository.StoreRepository;
import com.pos.minimarketpos.service.CashflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cashflows")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class CashflowController {

    private final CashflowService cashflowService;
    private final CategoryCashflowRepository categoryCashflowRepository;
    private final StoreRepository storeRepository;

    @GetMapping
    public ResponseEntity<List<CashflowDTO>> getAllCashflows() {
        return ResponseEntity.ok(cashflowService.getAllCashflows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashflowDTO> getCashflowById(@PathVariable Long id) {
        return ResponseEntity.ok(cashflowService.getCashflowById(id));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CashflowDTO>> getCashflowsByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(cashflowService.getCashflowsByStore(storeId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CashflowDTO>> getCashflowsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(cashflowService.getCashflowsByCategory(categoryId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CashflowDTO>> getCashflowsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(cashflowService.getCashflowsByDateRange(start, end));
    }

    @PostMapping
    public ResponseEntity<CashflowDTO> createCashflow(
            @RequestBody CashflowCreateRequest request,
            @AuthenticationPrincipal User user) {

        // Build Cashflow entity with relationships
        Cashflow cashflow = buildCashflowFromRequest(request);

        CashflowDTO created = cashflowService.createCashflow(cashflow, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CashflowDTO> updateCashflow(
            @PathVariable Long id,
            @RequestBody CashflowUpdateRequest request) {

        Cashflow cashflow = buildCashflowFromUpdateRequest(request);
        return ResponseEntity.ok(cashflowService.updateCashflow(id, cashflow));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCashflow(@PathVariable Long id) {
        cashflowService.deleteCashflow(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteCashflow(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        cashflowService.softDeleteCashflow(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/store/{storeId}/total")
    public ResponseEntity<BigDecimal> getTotalByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(cashflowService.getTotalByStore(storeId));
    }

    @GetMapping("/category/{categoryId}/total")
    public ResponseEntity<BigDecimal> getTotalByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(cashflowService.getTotalByCategory(categoryId));
    }

    @GetMapping("/date-range/total")
    public ResponseEntity<BigDecimal> getTotalByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(cashflowService.getTotalByDateRange(start, end));
    }

    @GetMapping("/net-cashflow")
    public ResponseEntity<BigDecimal> getNetCashflow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(cashflowService.getNetCashflow(start, end));
    }

    @GetMapping("/monthly-total")
    public ResponseEntity<BigDecimal> getMonthlyTotal(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(cashflowService.getMonthlyTotal(year, month));
    }

    @GetMapping("/inflow-total")
    public ResponseEntity<BigDecimal> getInflowTotal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(cashflowService.getInflowTotal(start, end));
    }

    @GetMapping("/outflow-total")
    public ResponseEntity<BigDecimal> getOutflowTotal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(cashflowService.getOutflowTotal(start, end));
    }

    // ========== HELPER METHODS ==========

    private Cashflow buildCashflowFromRequest(CashflowCreateRequest request) {
        CategoryCashflow category = categoryCashflowRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return Cashflow.builder()
                .date(request.getDate())
                .reference(request.getReference())
                .category(category)
                .store(store)
                .amount(request.getAmount())
                .lunas(request.getLunas() != null ? request.getLunas() : false)
                .note(request.getNote())
                .attachment(request.getAttachment())
                .flowType(request.getFlowType())
                .build();
    }

    private Cashflow buildCashflowFromUpdateRequest(CashflowUpdateRequest request) {
        Cashflow cashflow = new Cashflow();

        if (request.getDate() != null) {
            cashflow.setDate(request.getDate());
        }
        if (request.getReference() != null) {
            cashflow.setReference(request.getReference());
        }
        if (request.getCategoryId() != null) {
            CategoryCashflow category = categoryCashflowRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            cashflow.setCategory(category);
        }
        if (request.getStoreId() != null) {
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found"));
            cashflow.setStore(store);
        }
        if (request.getAmount() != null) {
            cashflow.setAmount(request.getAmount());
        }
        if (request.getLunas() != null) {
            cashflow.setLunas(request.getLunas());
        }
        if (request.getNote() != null) {
            cashflow.setNote(request.getNote());
        }
        if (request.getAttachment() != null) {
            cashflow.setAttachment(request.getAttachment());
        }
        if (request.getFlowType() != null) {
            cashflow.setFlowType(request.getFlowType());
        }

        return cashflow;
    }

    // ========== REQUEST DTOs ==========

    @lombok.Data
    public static class CashflowCreateRequest {
        private LocalDate date;
        private String reference;
        private Long categoryId;
        private Long storeId;
        private BigDecimal amount;
        private Boolean lunas;
        private String note;
        private String attachment;
        private Cashflow.CashflowType flowType;
    }

    @lombok.Data
    public static class CashflowUpdateRequest {
        private LocalDate date;
        private String reference;
        private Long categoryId;
        private Long storeId;
        private BigDecimal amount;
        private Boolean lunas;
        private String note;
        private String attachment;
        private Cashflow.CashflowType flowType;
    }
}

