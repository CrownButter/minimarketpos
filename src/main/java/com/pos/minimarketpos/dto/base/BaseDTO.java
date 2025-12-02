package com.pos.minimarketpos.dto.base;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseDTO {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}