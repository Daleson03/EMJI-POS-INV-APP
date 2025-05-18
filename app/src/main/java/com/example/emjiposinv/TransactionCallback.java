package com.example.emjiposinv;

public interface TransactionCallback {

    void onTransactionFound(TransactionHistory transactionHistory); // Called when the transaction exists

    void onTransactionNotFound(); // Called when no transaction is found
}
