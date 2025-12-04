package com.pos.minimarketpos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String footerText;
    private Integer status;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Boolean hasOpenRegister;

    public String getStatusDisplay() {
        return status != null && status == 1 ? "Active" : "Inactive";
    }
}