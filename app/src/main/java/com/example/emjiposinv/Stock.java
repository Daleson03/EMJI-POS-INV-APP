package com.example.emjiposinv;

public class Stock {

    private String product_id;
    private String batch_number;
    private String product_name;
    private String supplier;
    private String unit;
    private String quantity;
    private double unit_price;
    private double purchase_price;
    private String expiration_date;
    private String status;
    private String receive_date;
    private String receive_time;



    // Getters
    public String getProductId() { return product_id; }
    public String getBatchNumber() { return batch_number; }
    public String getProductName() { return product_name; }

    public String getSupplier() { return supplier; }

    public String getUnit() { return unit; }
    public String getQuantity() { return quantity; }
    public double getUnitPrice() { return unit_price; }
    public double getPurchasePrice() { return purchase_price; }
    public String getExpirationDate() { return expiration_date; }
    public String getStatus() { return status; }
    public String getReceiveDate() { return receive_date; }
    public String getReceiveTime() { return receive_time; }

}
