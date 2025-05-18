package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class ProductStock {
    @SerializedName("product_id")  // Ensure this matches your Supabase column name
    private String productId;

    @SerializedName("batch_number")  // Ensure this matches your Supabase column name
    private String batchNumber;

    @SerializedName("quantity")  // Ensure this matches your Supabase column name
    private int quantity;

    public String getProductId() { return productId; }
    public String getBatchNumber() { return batchNumber; }
    public int getQuantity() { return quantity; }
}
