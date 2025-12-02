package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.response.ExpenseDTO;
import com.pos.minimarketpos.model.CategoryExpense;
import com.pos.minimarketpos.model.Expense;
import com.pos.minimarketpos.repository.CategoryExpenseRepository;
import com.pos.minimarketpos.repository.ExpenseRepository;
import com.pos.minimarketpos.repository.StoreRepository;
import com.pos.minimarketpos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryExpenseRepository categoryExpenseRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.findAll()
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getExpenseById(Long id) {
        return mapToDTO(
                expenseRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Expense not found"))
        );
    }

    public List<ExpenseDTO> getExpensesByStore(Long storeId) {
        return expenseRepository.findByStoreId(storeId)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByCategory(Long categoryId) {
        return expenseRepository.findByCategoryId(categoryId)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByDateRange(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateRange(start, end)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByStatus(Boolean lunas) {
        return expenseRepository.findByLunas(lunas)
                .stream().map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO createExpense(Expense expense, Long userId) {
        expense.setCreatedBy(userId);
        expense.setCreatedDate(java.time.LocalDateTime.now());
        return mapToDTO(expenseRepository.save(expense));
    }

    public ExpenseDTO updateExpense(Long id, Expense updated) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        e.setDate(updated.getDate());
        e.setReference(updated.getReference());
        e.setAmount(updated.getAmount());
        e.setCategoryId(updated.getCategoryId());
        e.setStoreId(updated.getStoreId());
        e.setLunas(updated.getLunas());
        e.setNote(updated.getNote());
        e.setAttachment(updated.getAttachment());

        return mapToDTO(expenseRepository.save(e));
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public BigDecimal getMonthlyTotal(int year, int month) {
        BigDecimal amount = expenseRepository.getMonthlyTotal(year, month);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    public BigDecimal getTotalByStore(Long storeId) {
        return expenseRepository.findByStoreId(storeId)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalByCategory(Long categoryId) {
        return expenseRepository.findByCategoryId(categoryId)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalByDateRange(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateRange(start, end)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getUnpaidTotal() {
        return expenseRepository.findByLunas(false)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ExpenseDTO mapToDTO(Expense e) {
        ExpenseDTO dto = new ExpenseDTO();

        dto.setId(e.getId());
        dto.setDate(e.getDate());
        dto.setReference(e.getReference());
        dto.setCategoryId(e.getCategoryId());
        dto.setStoreId(e.getStoreId());
        dto.setAmount(e.getAmount());
        dto.setLunas(e.getLunas());
        dto.setNote(e.getNote());
        dto.setAttachment(e.getAttachment());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setCreatedDate(e.getCreatedDate());

        categoryExpenseRepository.findById(e.getCategoryId())
                .ifPresent(cat -> dto.setCategoryName(cat.getName()));

        storeRepository.findById(e.getStoreId())
                .ifPresent(st -> dto.setStoreName(st.getName()));

        userRepository.findById(e.getCreatedBy())
                .ifPresent(u -> dto.setCreatedByName(u.getFullName()));

        return dto;
    }
}
