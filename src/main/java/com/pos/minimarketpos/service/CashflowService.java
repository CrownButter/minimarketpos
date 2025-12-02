package com.pos.minimarketpos.service;

import com.pos.minimarketpos.controller.CashflowExportController.CashflowFilterRequest;
import com.pos.minimarketpos.dto.response.CashflowDTO;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.Cashflow;
import com.pos.minimarketpos.model.CategoryCashflow;
import com.pos.minimarketpos.model.Store;
import com.pos.minimarketpos.model.User;
import com.pos.minimarketpos.repository.CashflowRepository;
import com.pos.minimarketpos.repository.CategoryCashflowRepository;
import com.pos.minimarketpos.repository.StoreRepository;
import com.pos.minimarketpos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CashflowService {

    private final CashflowRepository cashflowRepository;
    private final CategoryCashflowRepository categoryCashflowRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public List<CashflowDTO> getAllCashflows() {
        log.debug("Fetching all cashflows");
        return cashflowRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CashflowDTO getCashflowById(Long id) {
        log.debug("Fetching cashflow by id: {}", id);
        Cashflow cashflow = cashflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cashflow not found with id: " + id));
        return mapToDTO(cashflow);
    }

    public List<CashflowDTO> getCashflowsByStore(Long storeId) {
        log.debug("Fetching cashflows by store id: {}", storeId);
        return cashflowRepository.findByStoreId(storeId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CashflowDTO> getCashflowsByCategory(Long categoryId) {
        log.debug("Fetching cashflows by category id: {}", categoryId);
        return cashflowRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CashflowDTO> getCashflowsByDateRange(LocalDate start, LocalDate end) {
        log.debug("Fetching cashflows between {} and {}", start, end);
        return cashflowRepository.findByDateRange(start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered cashflows based on multiple criteria
     */
    public List<CashflowDTO> getFilteredCashflows(CashflowFilterRequest filter) {
        log.debug("Fetching filtered cashflows: {}", filter);

        List<Cashflow> cashflows = cashflowRepository.findAll();

        return cashflows.stream()
                .filter(cf -> filter.getStoreId() == null || cf.getStore().getId().equals(filter.getStoreId()))
                .filter(cf -> filter.getCategoryId() == null || cf.getCategory().getId().equals(filter.getCategoryId()))
                .filter(cf -> filter.getStart() == null || !cf.getDate().isBefore(filter.getStart()))
                .filter(cf -> filter.getEnd() == null || !cf.getDate().isAfter(filter.getEnd()))
                .filter(cf -> filter.getLunas() == null || cf.getLunas().equals(filter.getLunas()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CashflowDTO createCashflow(Cashflow cashflow, Long userId) {
        log.info("Creating cashflow by user id: {}", userId);

        // Validate required fields
        validateCashflow(cashflow);

        // Load and set related entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        cashflow.setCreatedBy(user);
        cashflow.setCreatedDate(LocalDateTime.now());

        Cashflow saved = cashflowRepository.save(cashflow);
        log.info("Cashflow created successfully with id: {}", saved.getId());

        return mapToDTO(saved);
    }

    @Transactional
    public CashflowDTO updateCashflow(Long id, Cashflow updated) {
        log.info("Updating cashflow id: {}", id);

        Cashflow cf = cashflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cashflow not found with id: " + id));

        // Update fields if provided
        if (updated.getDate() != null) {
            cf.setDate(updated.getDate());
        }
        if (updated.getReference() != null) {
            cf.setReference(updated.getReference());
        }
        if (updated.getAmount() != null) {
            cf.setAmount(updated.getAmount());
        }
        if (updated.getCategory() != null) {
            CategoryCashflow category = categoryCashflowRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + updated.getCategory().getId()));
            cf.setCategory(category);
        }
        if (updated.getStore() != null) {
            Store store = storeRepository.findById(updated.getStore().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + updated.getStore().getId()));
            cf.setStore(store);
        }
        if (updated.getLunas() != null) {
            cf.setLunas(updated.getLunas());
        }
        if (updated.getNote() != null) {
            cf.setNote(updated.getNote());
        }
        if (updated.getAttachment() != null) {
            cf.setAttachment(updated.getAttachment());
        }
        if (updated.getFlowType() != null) {
            cf.setFlowType(updated.getFlowType());
        }

        Cashflow savedCf = cashflowRepository.save(cf);
        log.info("Cashflow updated successfully: {}", savedCf.getId());

        return mapToDTO(savedCf);
    }

    @Transactional
    public void deleteCashflow(Long id) {
        log.info("Deleting cashflow id: {}", id);

        Cashflow cashflow = cashflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cashflow not found with id: " + id));

        // Hard delete
        cashflowRepository.delete(cashflow);
        log.info("Cashflow deleted successfully: {}", id);
    }

    @Transactional
    public void softDeleteCashflow(Long id, Long userId) {
        log.info("Soft deleting cashflow id: {} by user: {}", id, userId);

        Cashflow cashflow = cashflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cashflow not found with id: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        cashflow.softDelete(user);
        cashflowRepository.save(cashflow);
        log.info("Cashflow soft deleted successfully: {}", id);
    }

    public BigDecimal getTotalByStore(Long storeId) {
        log.debug("Calculating total for store id: {}", storeId);
        return cashflowRepository.findByStoreId(storeId)
                .stream()
                .map(Cashflow::getSignedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalByCategory(Long categoryId) {
        log.debug("Calculating total for category id: {}", categoryId);
        return cashflowRepository.findByCategoryId(categoryId)
                .stream()
                .map(Cashflow::getSignedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalByDateRange(LocalDate start, LocalDate end) {
        log.debug("Calculating total between {} and {}", start, end);
        return cashflowRepository.findByDateRange(start, end)
                .stream()
                .map(Cashflow::getSignedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getNetCashflow(LocalDate start, LocalDate end) {
        log.debug("Calculating net cashflow between {} and {}", start, end);
        return getTotalByDateRange(start, end);
    }

    public BigDecimal getMonthlyTotal(int year, int month) {
        log.debug("Calculating monthly total for {}-{}", year, month);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return cashflowRepository.findByDateRange(start, end)
                .stream()
                .map(Cashflow::getSignedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getInflowTotal(LocalDate start, LocalDate end) {
        log.debug("Calculating inflow total between {} and {}", start, end);
        return cashflowRepository.findByDateRange(start, end)
                .stream()
                .filter(Cashflow::isInflow)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getOutflowTotal(LocalDate start, LocalDate end) {
        log.debug("Calculating outflow total between {} and {}", start, end);
        return cashflowRepository.findByDateRange(start, end)
                .stream()
                .filter(Cashflow::isOutflow)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validate cashflow before saving
     */
    private void validateCashflow(Cashflow cashflow) {
        if (cashflow.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (cashflow.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (cashflow.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (cashflow.getStore() == null) {
            throw new IllegalArgumentException("Store is required");
        }
        if (cashflow.getFlowType() == null) {
            throw new IllegalArgumentException("Flow type is required");
        }
    }

    /**
     * Map Cashflow entity to DTO
     */
    private CashflowDTO mapToDTO(Cashflow c) {
        CashflowDTO dto = new CashflowDTO();
        dto.setId(c.getId());
        dto.setDate(c.getDate());
        dto.setReference(c.getReference());
        dto.setAmount(c.getAmount());
        dto.setLunas(c.getLunas());
        dto.setNote(c.getNote());
        dto.setAttachment(c.getAttachment());
        dto.setCreatedDate(c.getCreatedDate());

        // Map Category
        if (c.getCategory() != null) {
            dto.setCategoryId(c.getCategory().getId());
            dto.setCategoryName(c.getCategory().getName());
        }

        // Map Store
        if (c.getStore() != null) {
            dto.setStoreId(c.getStore().getId());
            dto.setStoreName(c.getStore().getName());
        }

        // Map Created By User
        if (c.getCreatedBy() != null) {
            dto.setCreatedBy(c.getCreatedBy().getId());
            dto.setCreatedByName(c.getCreatedBy().getFullName());
        }

        return dto;
    }
}