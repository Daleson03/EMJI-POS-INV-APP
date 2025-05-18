package com.example.emjiposinv;

public class SalesReport {

private String transaction_number;
private String product_id;
private String batch_number;
private String product_name;
private String quantity;
private double purchase_price;
private double unit_price;
private double total_price;
private String supplier;
private String date;

public String getTransactionNumber() { return  transaction_number; }
public String getProductId() { return product_id; }
public String getBatchNumber() { return batch_number; }
public String getProductName() { return product_name; }

public String getQuantity() { return quantity; }
public double getPurchasePrice() { return purchase_price; }
public double getUnitPrice() { return unit_price; }
public double getTotalPrice() { return total_price; }

public String getSupplier() { return supplier; }

public String getDate() { return date; }


}
