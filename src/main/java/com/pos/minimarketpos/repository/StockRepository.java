package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByStoreIdAndProductId(Long storeId, Long productId);
    Optional<Stock> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
    List<Stock> findByProductId(Long productId);
    List<Stock> findByStoreId(Long storeId);
    List<Stock> findByWarehouseId(Long warehouseId);
}
