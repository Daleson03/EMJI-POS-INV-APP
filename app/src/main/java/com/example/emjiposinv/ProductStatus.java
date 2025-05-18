package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class ProductStatus {
    @SerializedName("product_id")
    private String productId;

    @SerializedName("batch_number")
    private String batchNumber;

    @SerializedName("product_name")
    private String productName;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("status")
    private String status;

    // Getters
    public String getProductId() { return productId; }
    public String getBatchNumber() { return batchNumber; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
}
