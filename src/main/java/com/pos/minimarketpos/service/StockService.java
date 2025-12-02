package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.request.StockUpdateRequest;
import com.pos.minimarketpos.dto.response.StockDTO;
import com.pos.minimarketpos.exception.InsufficientStockException;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.Stock;
import com.pos.minimarketpos.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(stock -> modelMapper.map(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProduct(Long productId) {
        return stockRepository.findByProductId(productId).stream()
                .map(stock -> modelMapper.map(stock, StockDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockDTO getStockByStoreAndProduct(Long storeId, Long productId) {
        Stock stock = stockRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for store: " + storeId + " and product: " + productId));
        return modelMapper.map(stock, StockDTO.class);
    }

    @Transactional(readOnly = true)
    public Integer getAvailableQuantity(Long storeId, Long productId) {
        return stockRepository.findByStoreIdAndProductId(storeId, productId)
                .map(Stock::getQuantity)
                .orElse(0);
    }

    @Transactional
    public void updateStock(StockUpdateRequest request) {
        // Update store stocks
        if (request.getStores() != null) {
            for (StockUpdateRequest.StockItem item : request.getStores()) {
                Stock stock = stockRepository
                        .findByStoreIdAndProductId(item.getStoreId(), request.getProductId())
                        .orElse(Stock.builder()
                                .productId(request.getProductId())
                                .storeId(item.getStoreId())
                                .build());

                stock.setQuantity(item.getQuantity());
                if (item.getPrice() != null) {
                    stock.setPrice(item.getPrice());
                }
                stockRepository.save(stock);
            }
        }

        // Update warehouse stocks
        if (request.getWarehouses() != null) {
            for (StockUpdateRequest.StockItem item : request.getWarehouses()) {
                Stock stock = stockRepository
                        .findByWarehouseIdAndProductId(item.getWarehouseId(), request.getProductId())
                        .orElse(Stock.builder()
                                .productId(request.getProductId())
                                .warehouseId(item.getWarehouseId())
                                .build());

                stock.setQuantity(item.getQuantity());
                stockRepository.save(stock);
            }
        }
    }

    @Transactional
    public void addStock(StockUpdateRequest request) {
        // Add to store stocks
        if (request.getStores() != null) {
            for (StockUpdateRequest.StockItem item : request.getStores()) {
                Stock stock = stockRepository
                        .findByStoreIdAndProductId(item.getStoreId(), request.getProductId())
                        .orElse(Stock.builder()
                                .productId(request.getProductId())
                                .storeId(item.getStoreId())
                                .quantity(0)
                                .build());

                stock.setQuantity(stock.getQuantity() + item.getQuantity());
                if (item.getPrice() != null) {
                    stock.setPrice(item.getPrice());
                }
                stockRepository.save(stock);
            }
        }

        // Add to warehouse stocks
        if (request.getWarehouses() != null) {
            for (StockUpdateRequest.StockItem item : request.getWarehouses()) {
                Stock stock = stockRepository
                        .findByWarehouseIdAndProductId(item.getWarehouseId(), request.getProductId())
                        .orElse(Stock.builder()
                                .productId(request.getProductId())
                                .warehouseId(item.getWarehouseId())
                                .quantity(0)
                                .build());

                stock.setQuantity(stock.getQuantity() + item.getQuantity());
                stockRepository.save(stock);
            }
        }
    }

    @Transactional
    public void reduceStock(Long storeId, Long productId, Integer quantity) {
        Stock stock = stockRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found for store: " + storeId + " and product: " + productId));

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + stock.getQuantity() + ", Required: " + quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
    }
}
