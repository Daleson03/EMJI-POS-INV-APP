package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class ProductStockAlert {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("batch_number")
    private String batchNumber;

    @SerializedName("product_name")
    private String productName;

    @SerializedName("supplier")
    private String supplier;

    @SerializedName("unit")
    private String unit;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("unit_price")
    private double unitPrice;

    @SerializedName("purchase_price")
    private double purchasePrice;

    @SerializedName("expiration_date")
    private String expirationDate;

    @SerializedName("status")
    private String status;

    public String getProductId() { return productId; }
    public String getBatchNumber() { return batchNumber; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
}
