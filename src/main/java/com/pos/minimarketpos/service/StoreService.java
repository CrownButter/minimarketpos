package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.response.StoreDTO;
import com.pos.minimarketpos.dto.response.StoreStatisticsDTO;
import com.pos.minimarketpos.exception.BadRequestException;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.Store;
import com.pos.minimarketpos.repository.RegisterRepository;
import com.pos.minimarketpos.repository.SaleRepository;
import com.pos.minimarketpos.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;
    private final RegisterRepository registerRepository;
    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public List<StoreDTO> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StoreDTO> getActiveStores() {
        return storeRepository.findByStatus(1).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StoreDTO getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        return mapToDTO(store);
    }

    @Transactional
    public StoreDTO createStore(Store store) {
        // Validation handled by DTO/Controller
        Store savedStore = storeRepository.save(store);
        log.info("Store created: {}", savedStore.getId());
        return mapToDTO(savedStore);
    }

    @Transactional
    public StoreDTO updateStore(Long id, Store updatedStore) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));

        store.setName(updatedStore.getName());
        store.setAddress(updatedStore.getAddress());
        store.setPhone(updatedStore.getPhone());
        store.setFooterText(updatedStore.getFooterText());

        Store savedStore = storeRepository.save(store);
        return mapToDTO(savedStore);
    }

    @Transactional
    public void deactivateStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + id));

        if (hasOpenRegister(id)) {
            throw new BadRequestException("Cannot deactivate store with open registers.");
        }

        store.setStatus(0);
        storeRepository.save(store);
    }

    @Transactional
    public StoreDTO activateStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + id));
        store.setStatus(1);
        return mapToDTO(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public boolean hasOpenRegister(Long storeId) {
        // Optimized: use count or exists instead of fetching entity if possible,
        // but current repo method returns Optional, acceptable.
        return registerRepository.findByStoreIdAndStatus(storeId, 1).isPresent();
    }

    // --- OPTIMIZED STATISTICS ---
    @Transactional(readOnly = true)
    public StoreStatisticsDTO getStoreStatistics(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        // Database Queries (Fast)
        Long totalRegisters = registerRepository.countByStoreId(storeId);
        Long openRegisters = registerRepository.countByStoreIdAndStatus(storeId, 1);

        BigDecimal totalSales = saleRepository.sumTotalSalesByStoreId(storeId);
        BigDecimal monthlySales = saleRepository.sumSalesByStoreIdAndDateRange(storeId, startOfMonth, now);

        Long totalTransactions = saleRepository.countTransactionsByStoreId(storeId);
        Long monthlyTransactions = saleRepository.countTransactionsByStoreIdAndDateRange(storeId, startOfMonth, now);

        // Null checks
        if (totalSales == null) totalSales = BigDecimal.ZERO;
        if (monthlySales == null) monthlySales = BigDecimal.ZERO;
        if (totalTransactions == null) totalTransactions = 0L;
        if (monthlyTransactions == null) monthlyTransactions = 0L;

        // Calculate Average
        BigDecimal avgTransaction = (totalTransactions > 0)
                ? totalSales.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return StoreStatisticsDTO.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .totalRegisters(totalRegisters)
                .openRegisters(openRegisters)
                .totalSales(totalSales)
                .monthlySales(monthlySales)
                .totalTransactions(totalTransactions)
                .monthlyTransactions(monthlyTransactions)
                .averageTransactionValue(avgTransaction)
                .isActive(store.getStatus() == 1)
                .build();
    }

    private StoreDTO mapToDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .footerText(store.getFooterText())
                .status(store.getStatus())
                .createdAt(store.getCreatedAt())
                .isActive(store.getStatus() == 1)
                // Warning: Calling DB inside loop. For single item view it's fine.
                // For lists, ideally fetch eagerly or ignore this field.
                .hasOpenRegister(hasOpenRegister(store.getId()))
                .build();
    }
}