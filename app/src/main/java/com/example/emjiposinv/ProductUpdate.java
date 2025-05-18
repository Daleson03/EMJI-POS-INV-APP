package com.example.emjiposinv;

public class ProductUpdate {
    private String productId;
    private String batchNumber;
    private int newQuantity;

    public ProductUpdate(String productId, String batchNumber, int newQuantity) {
        this.productId = productId;
        this.batchNumber = batchNumber;
        this.newQuantity = newQuantity;
    }

    public String getProductId() { return productId; }
    public String getBatchNumber() { return batchNumber; }
    public int getNewQuantity() { return newQuantity; }
}
