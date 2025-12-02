package com.pos.minimarketpos.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockUpdateRequest {
    private Long productId;
    private List<StockItem> stores;
    private List<StockItem> warehouses;

    @Data
    public static class StockItem {
        private Long storeId;
        private Long warehouseId;
        private Integer quantity;
        private BigDecimal price;
    }
}
