package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class ReturnItem {
    @SerializedName("transaction_number")
    private String transactionNumber;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("batch_number")
    private String batchNumber;

    @SerializedName("quantity")
    private int quantity;

    // Returns an empty string if the transaction number is null
    public String getTransactionNumber() {
        return transactionNumber != null ? transactionNumber : "";
    }

    // Returns an empty string if the productId is null
    public String getProductId() {
        return productId != null ? productId : "";
    }

    // Returns an empty string if the batchNumber is null
    public String getBatchNumber() {
        return batchNumber != null ? batchNumber : "";
    }

    public int getQuantity() {
        return quantity;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

