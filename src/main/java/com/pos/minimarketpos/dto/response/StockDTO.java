package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDTO {
    private Long id;
    private Long productId;
    private Long storeId;
    private Long warehouseId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String storeName;
    private String warehouseName;
}
