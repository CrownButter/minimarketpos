package com.pos.minimarketpos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreRequest {

    @NotBlank(message = "Store name is required")
    @Size(max = 100, message = "Store name must not exceed 100 characters")
    private String name;

    private String address;

    @Size(max = 20, message = "Phone number too long")
    private String phone;

    private String footerText;
}