package com.example.emjiposinv;

import com.google.gson.annotations.SerializedName;

public class Supplier {
    @SerializedName("SupplierID")  // Ensure it matches column name in Supabase
    private int supplierID;

    @SerializedName("SupplierName")  // Ensure it matches column name in Supabase
    private String supplierName;

    @SerializedName("ContactPerson") // Ensure it matches column name in Supabase
    private String contactPerson;

    @SerializedName("Phone") // Ensure it matches column name in Supabase
    private String phone;

    @SerializedName("Email") // Ensure it matches column name in Supabase
    private String email;

    @SerializedName("CutoffTime") // Ensure it matches column name in Supabase
    private String cutoffTime;

    public Supplier(int supplierID, String supplierName, String contactPerson, String phone, String email, String cutoffTime) {
        this.supplierID = supplierID;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.cutoffTime = cutoffTime;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCutoffTime() {
        return cutoffTime;
    }

    @Override
    public String toString() {
        if (supplierID == 0) {
            return supplierName; // Show only "Select a Supplier"
        }
        return supplierID + " - " + supplierName;
    }
}


