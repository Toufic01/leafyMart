package com.leafymart.Model;

public class OrderModel {
    private int orderId;
    private String date;
    private String status;

    // Add constructor
    public OrderModel(int orderId, String date, String status) {
        this.orderId = orderId;
        this.date = date;
        this.status = status;
    }

    // Getters
    public int getOrderId() {
        return orderId;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    // Setters (optional)
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
