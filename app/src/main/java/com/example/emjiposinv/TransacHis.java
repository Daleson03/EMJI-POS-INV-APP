package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class TransacHis {
    @SerializedName("transaction_number")  // Match actual column name
    private String transaction_number;
    @SerializedName("discount")  // Match actual column name
    private double discount;

    public String getTransactionNumber() { return transaction_number; }
    public double getDiscount() { return discount; }

    public void setTransactionNumber(String transaction_number) { this.transaction_number = transaction_number; }
    public void setDiscount(double discount) { this.discount = discount; }
}