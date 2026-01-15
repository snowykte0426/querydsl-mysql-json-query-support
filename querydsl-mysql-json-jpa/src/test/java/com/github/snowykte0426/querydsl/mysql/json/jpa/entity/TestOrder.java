package com.github.snowykte0426.querydsl.mysql.json.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test entity representing an order with JSON order data and shipping info.
 * Named TestOrder to avoid conflict with SQL reserved word 'ORDER'.
 */
@Entity
@Table(name = "orders")
public class TestOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 50)
    private String status;

    @Column(name = "order_data", columnDefinition = "JSON")
    private String orderData;

    @Column(name = "shipping_info", columnDefinition = "JSON")
    private String shippingInfo;

    @Column(name = "payment_info", columnDefinition = "JSON")
    private String paymentInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TestOrder() {
    }

    public TestOrder(String orderNumber, BigDecimal totalAmount) {
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
    }

    public TestOrder(String orderNumber, Long userId, BigDecimal totalAmount) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderData() {
        return orderData;
    }

    public void setOrderData(String orderData) {
        this.orderData = orderData;
    }

    public String getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(String shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "TestOrder{" +
               "id=" + id +
               ", orderNumber='" + orderNumber + '\'' +
               ", userId=" + userId +
               ", totalAmount=" + totalAmount +
               ", status='" + status + '\'' +
               '}';
    }
}
