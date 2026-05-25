package com.paymentmatchingtool.entity;

import com.paymentmatchingtool.enums.MatchStatus;
import com.paymentmatchingtool.enums.ResolutionSide;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "match_results")
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String orderId;

    @Column(precision = 19, scale = 2)
    private BigDecimal systemAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal providerAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status;

    @Column(nullable = false)
    private boolean resolved;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ResolutionSide resolutionSide;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected MatchResult() {
    }

    public MatchResult(String orderId, BigDecimal systemAmount, BigDecimal providerAmount, String currency, MatchStatus status) {
        this.orderId = orderId;
        this.systemAmount = systemAmount;
        this.providerAmount = providerAmount;
        this.currency = currency;
        this.status = status;
        this.resolved = false;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getSystemAmount() {
        return systemAmount;
    }

    public BigDecimal getProviderAmount() {
        return providerAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public boolean isResolved() {
        return resolved;
    }

    public ResolutionSide getResolutionSide() {
        return resolutionSide;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void resolve(ResolutionSide resolutionSide) {
        this.resolved = true;
        this.resolutionSide = resolutionSide;
    }
}
