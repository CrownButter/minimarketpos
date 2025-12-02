package com.pos.minimarketpos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String adresses;
    private String phone;

    @Column(name = "footer_text")
    private String footerText;

    private Integer status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}