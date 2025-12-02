package com.pos.minimarketpos.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyname;
    private String phone;
    private String email;
    private String logo;
    private String currency;
    private Integer decimals;
    private String timezone;
    private String language;

    @Column(name = "receiptheader", columnDefinition = "TEXT")
    private String receiptheader;

    @Column(name = "receiptfooter", columnDefinition = "TEXT")
    private String receiptfooter;

    @Column(name = "stripe_secret_key")
    private String stripeSecretKey;

    @Column(name = "stripe_public_key")
    private String stripePublicKey;
}