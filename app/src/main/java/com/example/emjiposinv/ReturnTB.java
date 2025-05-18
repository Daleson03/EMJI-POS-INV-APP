package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class ReturnTB {

    @SerializedName("transaction_number")
    private String transactionNumber;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("batch_number")
    private String batchNumber;

    @SerializedName("product_name")
    private String productName;

    @SerializedName("supplier")
    private String supplier;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("return_reason")
    private String returnReason;

    @SerializedName("return_date")
    private String returnDate;

    // Safe Getters
    public String getTransactionNumber() {
        return transactionNumber != null ? transactionNumber : "";
    }

    public String getProductId() {
        return productId != null ? productId : "";
    }

    public String getBatchNumber() {
        return batchNumber != null ? batchNumber : "";
    }

    public String getProductName() {
        return productName != null ? productName : "";
    }

    public String getSupplier() {
        return supplier != null ? supplier : "";
    }

    public int getQuantity() {
        return quantity;
    }

    public String getReturnReason() {
        return returnReason != null ? returnReason : "";
    }

    public String getReturnDate() {
        return returnDate != null ? returnDate : "";
    }

    // Setters
    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    // Helper method for normalization
    private String normalize(String input) {
        return input == null ? "" : input.trim().toLowerCase();
    }

    // Returns a normalized key used for matching with SalesRep
    public String getNormalizedKey() {
        return normalize(getTransactionNumber()) + "|" +
                normalize(getProductId()) + "|" +
                normalize(getBatchNumber());
    }
}
