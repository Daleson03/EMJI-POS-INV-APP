package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class SupplierNotif {

    @SerializedName("SupplierID")
    private String supplierId;

    @SerializedName("SupplierName")
    private String supplierName;

    @SerializedName("CutoffTime")
    private String cutoffTime;

    // Add other fields if needed

    public void Supplier(String supplierName, String cutoffTime) {
        this.supplierName = supplierName;
        this.cutoffTime = cutoffTime;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getCutoffTime() {
        return cutoffTime;
    }
}
