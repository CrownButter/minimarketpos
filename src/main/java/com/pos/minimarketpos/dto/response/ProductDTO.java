package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long id;
    private String code;
    private String name;
    private String category;
    private BigDecimal cost;
    private BigDecimal price;
    private String description;
    private String tax;
    private Integer taxmethod;
    private Integer alertqt;
    private String color;
    private String photo;
    private String photothumb;
    private String supplier;
    private String unit;
    private String type;
    private String hStores;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer stock;
}

