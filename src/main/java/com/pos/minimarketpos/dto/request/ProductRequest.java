package com.pos.minimarketpos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String category;

    @NotNull
    private BigDecimal cost;

    @NotNull
    private BigDecimal price;

    private String description;
    private String tax;
    private Integer taxmethod;
    private Integer alertqt;
    private String color;
    private String supplier;
    private String unit;

    @NotBlank
    private String type;
}
