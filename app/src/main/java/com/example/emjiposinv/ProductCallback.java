package com.example.emjiposinv;

public interface ProductCallback {
    void onProductFound(Product product); // Called when the product exists
    void onProductNotFound(); // Called when no product is found
}
