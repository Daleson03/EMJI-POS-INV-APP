package com.example.emjiposinv;

public class TransactionHistory {
    private String transaction_number;
    private double subtotal;
    private double discount;
    private double total_amount;
    private double change;
    private String date;
    private String payment_method;
    private double cash;
    private String reference_number;

    public String getTransactionNumber() { return transaction_number; }
    public double getSubtotal() { return subtotal; }
    public double getDiscount() { return discount; }
    public double getTotalAmount() { return total_amount; }
    public double getChange() { return change; }

    public String getDate() { return date; }
    public String getPaymentMethod() { return payment_method; }
    public double getCash() { return cash; }
    public String getReferenceNumber() { return reference_number; }



}
