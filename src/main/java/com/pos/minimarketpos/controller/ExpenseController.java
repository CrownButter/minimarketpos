package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.response.ExpenseDTO;
import com.pos.minimarketpos.model.Expense;
import com.pos.minimarketpos.model.User;
import com.pos.minimarketpos.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(expenseService.getExpensesByStore(storeId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(expenseService.getExpensesByCategory(categoryId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(start, end));
    }

    @GetMapping("/status")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByStatus(
            @RequestParam Boolean lunas) {
        return ResponseEntity.ok(expenseService.getExpensesByStatus(lunas));
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(
            @RequestBody Expense expense,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Cast UserDetails to User to get the ID
        User user = (User) userDetails;
        return ResponseEntity.ok(expenseService.createExpense(expense, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable Long id,
            @RequestBody Expense expense) {
        return ResponseEntity.ok(expenseService.updateExpense(id, expense));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok("Expense deleted successfully");
    }

    @GetMapping("/monthly-total")
    public ResponseEntity<BigDecimal> getMonthlyTotal(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getMonthlyTotal(year, month));
    }

    @GetMapping("/store/{storeId}/total")
    public ResponseEntity<BigDecimal> getTotalByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(expenseService.getTotalByStore(storeId));
    }

    @GetMapping("/category/{categoryId}/total")
    public ResponseEntity<BigDecimal> getTotalByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(expenseService.getTotalByCategory(categoryId));
    }

    @GetMapping("/date-range/total")
    public ResponseEntity<BigDecimal> getTotalByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(expenseService.getTotalByDateRange(start, end));
    }

    @GetMapping("/unpaid/total")
    public ResponseEntity<BigDecimal> getUnpaidTotal() {
        return ResponseEntity.ok(expenseService.getUnpaidTotal());
    }
}