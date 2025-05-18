package com.example.emjiposinv;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import android.os.Handler;

import com.google.gson.Gson;


public class DatabaseHelper {
    private final SupabaseAuthApi supabaseApi;
    private final Context context;

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ" +
            "hbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYT" +
            "eI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsI" +
            "nJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU" +
            "0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";
    private static final String CONTENT_TYPE = "application/json";

    public DatabaseHelper(Context context) {
        this.supabaseApi = RetrofitClient.getSupabaseApi();
        this.context = context;
    }


    private AddUpdateSupFragment fragment;

    public DatabaseHelper(Context context, AddUpdateSupFragment fragment) {
        this.supabaseApi = RetrofitClient.getSupabaseApi();
        this.context = context;
        this.fragment = fragment; // Store the reference of the fragment
    }


    // Now you can call fetchSuppliers from the fragment
    public void fetchSuppliersFromFragment() {
        if (fragment != null) {
            fragment.fetchSuppliers(); // Call fetchSuppliers in the fragment
        }
    }
    public void insertSupplier(String suppliername,String contactperson,String phone,String email,String cutofftime) {
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("SupplierName", suppliername);
        supplier.put("ContactPerson", contactperson);
        supplier.put("Phone", phone);
        supplier.put("Email", email);
        supplier.put("CutoffTime", cutofftime);

        supabaseApi.insertSupplier(supplier).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Supplier Inserted Successfully!", Toast.LENGTH_SHORT).show();
                    fetchSuppliersFromFragment();
                } else {
                    Toast.makeText(context, "Insert Failed: " + response.errorBody(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    public void insertProduct(String batchNumber, String name, String supplier, String unit,
                              String productid, int quantity, double unitPrice, double purchasePrice,
                              String expirationDate, String status) {

        // Prepare the product data
        Map<String, Object> product = new HashMap<>();
        product.put("product_id", productid);      // product_id as a string
        product.put("batch_number", batchNumber);  // batch_number as a string, can be empty if not available
        product.put("product_name", name);         // product_name as a string
        product.put("supplier", supplier);         // supplier as a string
        product.put("unit", unit);                 // unit as a string
        product.put("quantity", quantity);         // quantity as an integer
        product.put("unit_price", unitPrice);      // unit_price as a double (DECIMAL in DB)
        product.put("purchase_price", purchasePrice); // purchase_price as a double (DECIMAL in DB)

        // Ensure expiration_date is valid, or set it to null if not available
        if (expirationDate == null || expirationDate.isEmpty()) {
            product.put("expiration_date", null);  // If no expiration date, set it to null
        } else {
            product.put("expiration_date", expirationDate); // Otherwise, use the provided expiration date
        }

        product.put("status", status);             // status as a string

        // Insert into the database via the API
        supabaseApi.insertProduct(product).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Product Inserted Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API Error", errorBody);  // Log the error body for debugging
                        Toast.makeText(context, "Insert Failed: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void updateSupplier(int supplierID, String supplierName, String contactPerson, String phone, String email, String cutoffTime) {
        if (supplierID == -1) { // Check if a Supplier is selected
            Toast.makeText(context, "Please select a valid supplier!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateSupplier = new HashMap<>();
        updateSupplier.put("SupplierName", supplierName);
        updateSupplier.put("ContactPerson", contactPerson);
        updateSupplier.put("Phone", phone);
        updateSupplier.put("Email", email);
        updateSupplier.put("CutoffTime", cutoffTime);

        String filter = "eq." + supplierID; // Ensure "eq." prefix for filtering

        supabaseApi.updateSupplier(filter, updateSupplier).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Supplier Updated Successfully!", Toast.LENGTH_SHORT).show();
                    fetchSuppliersFromFragment();
                } else {
                    try {
                        String errorResponse = response.errorBody().string();
                        Toast.makeText(context, "Update Failed: " + errorResponse, Toast.LENGTH_LONG).show();
                        Log.e("SupabaseUpdate", "Error: " + errorResponse);
                    } catch (Exception e) {
                        Toast.makeText(context, "Update Failed: Unable to read error", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void updateProductStock(String expirationDate,String productId,String batchnum, int Quantity) {
        Map<String, Object> updatedProduct = new HashMap<>();
        updatedProduct.put("batch_number", batchnum);
        updatedProduct.put("expiration_date", expirationDate);
        updatedProduct.put("quantity", Quantity);


        String filter = "eq." + productId; // ✅ Correct Supabase filter

        supabaseApi.updateProductStock(filter, updatedProduct).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Stock Updated Successfully!", Toast.LENGTH_SHORT).show();

                    // ✅ Call insertStockHistoryForProduct after updating stock
                    insertStockHistoryForProduct(productId, batchnum, expirationDate, Quantity);

                } else {
                    try {
                        // ✅ Print error response for debugging
                        Toast.makeText(context, "Update Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Update Failed: Unable to read error", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
    private void insertStockHistoryForBatch(String productId, String batchnum, String expirationDate, int addedQuantity) {
        String filterProductId = "eq." + productId;

        // 🔍 Fetch product details before inserting into history
        supabaseApi.getProductById(filterProductId, API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> productData = response.body().get(0); // ✅ Get product details


                    // ✅ Get current date and time
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()); // ⏰ 12-hour format with AM/PM
                    String receiveDate = dateFormat.format(new Date()); // 📅 Get today's date
                    String receiveTime = timeFormat.format(new Date()); // ⏰ Get current time (AM/PM format)

                    // ✅ Create history record for batch update
                    Map<String, Object> historyData = new HashMap<>();
                    historyData.put("product_id", productId);
                    historyData.put("batch_number", batchnum);
                    historyData.put("expiration_date", expirationDate);
                    historyData.put("quantity", addedQuantity); // ✅ Save only the added quantity
                    historyData.put("product_name", productData.get("product_name"));
                    historyData.put("supplier", productData.get("supplier"));
                    historyData.put("unit", productData.get("unit"));
                    historyData.put("unit_price", productData.get("unit_price"));
                    historyData.put("purchase_price", productData.get("purchase_price"));
                    historyData.put("status", productData.get("status"));
                    historyData.put("receive_date", receiveDate); // ✅ Add current date
                    historyData.put("receive_time", receiveTime); // ✅ Add current time

                    // ✅ Insert into StockHistoryTB
                    supabaseApi.insertStockHistory(historyData, API_KEY, AUTH_TOKEN).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "📜 Batch stock history saved! Added Qty: " + addedQuantity, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    Toast.makeText(context, "Batch History Insert Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Batch History Insert Failed: Unable to read error", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(context, "Batch History Insert Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Toast.makeText(context, "Product ID not found for batch history!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(context, "Error fetching product for batch history: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void insertStockHistoryForProduct(String productId, String batchnum, String expirationDate, int addedQuantity) {
        String filterProductId = "eq." + productId;

        // 🔍 Fetch product details before inserting into history
        supabaseApi.getProductById(filterProductId, API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> productData = response.body().get(0); // ✅ Get product details

                    // ✅ Get current date and time
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()); // ⏰ 12-hour format with AM/PM
                    String receiveDate = dateFormat.format(new Date()); // 📅 Get today's date
                    String receiveTime = timeFormat.format(new Date()); // ⏰ Get current time (AM/PM format)

                    // ✅ Create history record for product update (Now includes batch_number)
                    Map<String, Object> historyData = new HashMap<>();
                    historyData.put("product_id", productId);
                    historyData.put("batch_number", batchnum); // ✅ Now includes batch number
                    historyData.put("expiration_date", expirationDate);
                    historyData.put("quantity", addedQuantity); // ✅ Save only the added quantity
                    historyData.put("product_name", productData.get("product_name"));
                    historyData.put("supplier", productData.get("supplier"));
                    historyData.put("unit", productData.get("unit"));
                    historyData.put("unit_price", productData.get("unit_price"));
                    historyData.put("purchase_price", productData.get("purchase_price"));
                    historyData.put("status", productData.get("status"));
                    historyData.put("receive_date", receiveDate); // ✅ Add current date
                    historyData.put("receive_time", receiveTime); // ✅ Add current time

                    // ✅ Insert into StockHistoryTB
                    supabaseApi.insertStockHistory(historyData, API_KEY, AUTH_TOKEN).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "📜 Product stock history saved! Added Qty: " + addedQuantity, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    Toast.makeText(context, "Product History Insert Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Product History Insert Failed: Unable to read error", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(context, "Product History Insert Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Toast.makeText(context, "Product ID not found for product history!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(context, "Error fetching product for product history: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertStockHistoryForNewBatch(String productId, String batchnum, String expirationDate, int addedQuantity, Map<String, Object> productData) {

        // ✅ Get current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()); // ⏰ 12-hour format with AM/PM
        String receiveDate = dateFormat.format(new Date()); // 📅 Get today's date
        String receiveTime = timeFormat.format(new Date()); // ⏰ Get current time (AM/PM format)

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("product_id", productId);
        historyData.put("batch_number", batchnum);
        historyData.put("expiration_date", expirationDate);
        historyData.put("quantity", addedQuantity); // ✅ Save only the added quantity
        historyData.put("product_name", productData.get("product_name"));
        historyData.put("supplier", productData.get("supplier"));
        historyData.put("unit", productData.get("unit"));
        historyData.put("unit_price", productData.get("unit_price"));
        historyData.put("purchase_price", productData.get("purchase_price"));
        historyData.put("status", productData.get("status"));
        historyData.put("receive_date", receiveDate); // ✅ Add current date
        historyData.put("receive_time", receiveTime); // ✅ Add current time

        supabaseApi.insertStockHistory(historyData, API_KEY, AUTH_TOKEN).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "📜 Stock history saved for new batch!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Toast.makeText(context, "Stock History Insert Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Stock History Insert Failed: Unable to read error", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Stock History Insert Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void updateProductStockwithBatch(String expirationDate, String productId, String batchnum, int Quantity) {
        String filterProductId = "eq." + productId;
        String filterBatchNum = "eq." + batchnum;

        Toast.makeText(context, "Fetching batch: " + productId + " - " + batchnum, Toast.LENGTH_LONG).show();

        supabaseApi.getBatchByProduct(filterProductId, filterBatchNum, API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    if (!response.body().isEmpty()) {
                        Map<String, Object> batchData = response.body().get(0);

                        // 🔍 Check if 'quantity' exists in response
                        if (!batchData.containsKey("quantity") || batchData.get("quantity") == null) {
                            Toast.makeText(context, "Error: 'quantity' field missing!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 🔍 Check the actual value of 'quantity'
                        Object quantityValue = batchData.get("quantity");
                        Toast.makeText(context, "Existing Qty: " + quantityValue, Toast.LENGTH_LONG).show();

                        int existingQuantity = 0;
                        if (quantityValue instanceof Number) {
                            existingQuantity = ((Number) quantityValue).intValue(); // ✅ Convert safely if it's a number
                        } else {
                            try {
                                existingQuantity = Integer.parseInt(quantityValue.toString()); // ✅ Convert only if needed
                            } catch (NumberFormatException e) {
                                Toast.makeText(context, "Error parsing quantity: " + quantityValue, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        int updatedQuantity = existingQuantity + Quantity;
                        Toast.makeText(context, "Updated Qty: " + updatedQuantity, Toast.LENGTH_LONG).show();

                        Map<String, Object> updatedStock = new HashMap<>();
                        updatedStock.put("quantity", updatedQuantity);

                        supabaseApi.updateProductStockwithBatch(filterProductId, filterBatchNum, API_KEY, AUTH_TOKEN, updatedStock).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(context, "Stock Updated! New Qty: " + updatedQuantity, Toast.LENGTH_LONG).show();

                                    // ✅ Call insertStockHistoryForBatch after updating stock
                                    insertStockHistoryForBatch(productId, batchnum, expirationDate, Quantity);
                                } else {
                                    try {
                                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                        Toast.makeText(context, "Update Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Toast.makeText(context, "Failed to read error!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(context, "Update Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {
                        Toast.makeText(context, "Batch not found, inserting new batch...", Toast.LENGTH_LONG).show();
                        fetchProductDetailsAndInsert(productId, batchnum, expirationDate, Quantity);
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch batch: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(context, "API Call Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }




    private void fetchProductDetailsAndInsert(String productId, String batchnum, String expirationDate, int Quantity) {
        String filterProductId = "eq." + productId;

        supabaseApi.getProductById(filterProductId, API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> productData = response.body().get(0);

                    if (!productData.containsKey("product_name")) {
                        Toast.makeText(context, "Error: Product details missing!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> newStock = new HashMap<>();
                    newStock.put("product_id", productId);
                    newStock.put("batch_number", batchnum);
                    newStock.put("expiration_date", expirationDate);
                    newStock.put("quantity", Quantity);
                    newStock.put("product_name", productData.get("product_name"));
                    newStock.put("supplier", productData.get("supplier"));
                    newStock.put("unit_price", productData.get("unit_price"));
                    newStock.put("purchase_price", productData.get("purchase_price"));
                    newStock.put("status", productData.get("status"));
                    newStock.put("unit", productData.get("unit"));

                    supabaseApi.insertNewStock(newStock, API_KEY, AUTH_TOKEN).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "New Batch Added Successfully!", Toast.LENGTH_SHORT).show();

                                // ✅ Call stock history method for new batch
                                insertStockHistoryForNewBatch(productId, batchnum, expirationDate, Quantity, productData);
                            } else {
                                try {
                                    Toast.makeText(context, "Insert Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Insert Failed: Unable to read error", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Product ID not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    public void updateProduct(String productId, String name, String supplier, double unitPrice, double purchasePrice, String status) {
        Map<String, Object> updatedProduct = new HashMap<>();
        updatedProduct.put("product_name", name);
        updatedProduct.put("supplier", supplier);
        updatedProduct.put("unit_price", unitPrice);
        updatedProduct.put("purchase_price", purchasePrice);
        updatedProduct.put("status", status);

        String filter = "eq." + productId; // ✅ Correct Supabase filter

        supabaseApi.updateProduct(filter, updatedProduct).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Product Updated Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        // ✅ Print error response for debugging
                        Toast.makeText(context, "Update Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Update Failed: Unable to read error", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateProductwithBatch(String productId, String batch, String name, String supplier, double unitPrice, double purchasePrice, String status) {
        Map<String, Object> updatedProduct = new HashMap<>();
        updatedProduct.put("product_name", name);
        updatedProduct.put("supplier", supplier);
        updatedProduct.put("unit_price", unitPrice);
        updatedProduct.put("purchase_price", purchasePrice);
        updatedProduct.put("status", status);

        String productidfilter = "eq." + productId; // ✅ Correct Supabase filter
        String batchnumberfilter = "eq." + batch;


        supabaseApi.updateProductwithbatch(productidfilter, batchnumberfilter, updatedProduct).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Product Updated Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        // ✅ Print error response for debugging
                        Toast.makeText(context, "Update Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Update Failed: Unable to read error", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    public void getProductByBarcode(String barcode, ProductCallback callback) {
        try {
            String productId = barcode.trim(); // ✅ Ensure barcode is an integer

            Log.d("Supabase", "🔍 Searching for Product ID: " + productId);

            supabaseApi.getProductByBarcode("eq." + productId, "*", API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                    Log.d("Supabase", "Response Code: " + response.code());
                    Log.d("Supabase", "Request URL: " + call.request().url().toString());

                    if (response.isSuccessful() && response.body() != null) {
                        List<Product> products = response.body();
                        Log.d("Supabase", "✅ Products Found: " + products.size());

                        if (!products.isEmpty()) {
                            Log.d("Supabase", "🎉 Product Found: " + products.get(0).getProductName());
                            Toast.makeText(context, "Product Found: " + products.get(0).getProductName(), Toast.LENGTH_LONG).show();
                            callback.onProductFound(products.get(0));
                        } else {
                            Log.e("Supabase", "❌ Product list is empty in response!");
                            Toast.makeText(context, "❌ Product not found!", Toast.LENGTH_LONG).show();
                            callback.onProductNotFound();
                        }
                    } else {
                        Log.e("Supabase", "❌ API Error: " + response.errorBody());
                        Toast.makeText(context, "❌ API Error!", Toast.LENGTH_LONG).show();
                        callback.onProductNotFound();
                    }
                }

                @Override
                public void onFailure(Call<List<Product>> call, Throwable t) {
                    Log.e("Supabase", "❌ API Call Failed", t);
                    Toast.makeText(context, "❌ API Call Failed", Toast.LENGTH_LONG).show();
                    callback.onProductNotFound();
                }
            });

        } catch (NumberFormatException e) {
            Log.e("Supabase", "❌ Invalid Barcode Format", e);
            Toast.makeText(context, "❌ Invalid Barcode Format", Toast.LENGTH_LONG).show();
            callback.onProductNotFound();
        }
    }

    public void getTransactionByBarcode(String barcode, TransactionCallback callback) {
        try {
            String transactionnum = barcode.trim(); // ✅ Ensure barcode is an integer

            Log.d("Supabase", "🔍 Searching for Transaction Number: " + transactionnum);

            supabaseApi.getTransactionByBarcode("eq." + transactionnum, "*", API_KEY, AUTH_TOKEN).enqueue(new Callback<List<TransactionHistory>>() {
                @Override
                public void onResponse(Call<List<TransactionHistory>> call, Response<List<TransactionHistory>> response) {
                    Log.d("Supabase", "Response Code: " + response.code());
                    Log.d("Supabase", "Request URL: " + call.request().url().toString());

                    if (response.isSuccessful() && response.body() != null) {
                        List<TransactionHistory> transactionhistory = response.body();
                        Log.d("Supabase", "✅ Transaction Found: " + transactionhistory.size());

                        if (!transactionhistory.isEmpty()) {
                            Log.d("Supabase", "🎉 Transaction Found: " + transactionhistory.get(0).getTransactionNumber());
                            Toast.makeText(context, "Transaction Found: " + transactionhistory.get(0).getTransactionNumber(), Toast.LENGTH_LONG).show();
                            callback.onTransactionFound(transactionhistory.get(0));
                        } else {
                            Log.e("Supabase", "❌ Transaction list is empty in response!");
                            Toast.makeText(context, "❌ Transaction not found!", Toast.LENGTH_LONG).show();
                            callback.onTransactionNotFound();
                        }
                    } else {
                        Log.e("Supabase", "❌ API Error: " + response.errorBody());
                        Toast.makeText(context, "❌ API Error!", Toast.LENGTH_LONG).show();
                        callback.onTransactionNotFound();
                    }
                }

                @Override
                public void onFailure(Call<List<TransactionHistory>> call, Throwable t) {
                    Log.e("Supabase", "❌ API Call Failed", t);
                    Toast.makeText(context, "❌ API Call Failed", Toast.LENGTH_LONG).show();
                    callback.onTransactionNotFound();
                }
            });

        } catch (NumberFormatException e) {
            Log.e("Supabase", "❌ Invalid Barcode Format", e);
            Toast.makeText(context, "❌ Invalid Barcode Format", Toast.LENGTH_LONG).show();
            callback.onTransactionNotFound();
        }
    }

    public void fetchAndUpdateStock(List<ProductUpdate> updatesList, StockUpdateListener listener) {
        if (updatesList.isEmpty()) {
            listener.onStockUpdateFailed("⚠️ No products found!");
            return;
        }


        Map<String, String> queryMap = new HashMap<>();

        if (updatesList.size() == 1) {
            // ✅ Single product fetch (No changes here)
            ProductUpdate update = updatesList.get(0);
            queryMap.put("product_id", "eq." + update.getProductId());
            queryMap.put("batch_number", "eq." + update.getBatchNumber());
        } else {
            // ✅ Multiple product fetches (Fixed query format)
            List<String> orConditions = new ArrayList<>();

            for (ProductUpdate update : updatesList) {
                orConditions.add("and(product_id.eq." + update.getProductId() +
                        ",batch_number.eq." + update.getBatchNumber() + ")");
            }

            // ✅ Properly format "or" condition
            queryMap.put("or", "(" + String.join(",", orConditions) + ")");
        }


        Log.d("DatabaseHelper", "🔍 Supabase QueryMap: " + queryMap.toString());

        supabaseApi.getStock(API_KEY, AUTH_TOKEN, CONTENT_TYPE, queryMap)
                .enqueue(new Callback<List<ProductStock>>() {
                    @Override
                    public void onResponse(Call<List<ProductStock>> call, Response<List<ProductStock>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ProductStock> stockList = response.body();
                            Toast.makeText(context, "✅ Stock fetched: " + stockList.size(), Toast.LENGTH_LONG).show();
                            if (!stockList.isEmpty()) {
                                updateStockAfterFetch(stockList, updatesList, listener);
                            } else {
                                listener.onStockUpdateFailed("❌ No stock data received!");
                            }
                        } else {
                            String errorBody = "";
                            try {
                                errorBody = response.errorBody().string();
                            } catch (IOException e) {
                                errorBody = "Unknown error";
                            }

                            String errorMsg = "❌ HTTP " + response.code() + " - Failed to fetch stock! " + errorBody;
                            Log.e("DatabaseHelper", errorMsg);
                            saveErrorLogToFile(errorMsg);
                            listener.onStockUpdateFailed(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProductStock>> call, Throwable t) {
                        Toast.makeText(context, "❌ Error fetching stock!", Toast.LENGTH_LONG).show();
                    }
                });
    }




    private void saveErrorLogToFile(String errorMessage) {
        if (context == null) {
            Log.e("DatabaseHelper", "❌ Context is null! Cannot save log.");
            return;
        }

        try {
            // Save the error log in the Downloads folder
            File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "error_log.txt");

            FileWriter writer = new FileWriter(logFile, true); // Append mode
            writer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())) // Timestamp
                    .append(" - ")
                    .append(errorMessage)
                    .append("\n");
            writer.close();

            Log.d("DatabaseHelper", "📄 Error log saved at: " + logFile.getAbsolutePath());

            // Show confirmation in UI
            Toast.makeText(context, "Error log saved: " + logFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("DatabaseHelper", "❌ Failed to save error log to file", e);
        }
    }



    // Interface for callback
    public interface StockUpdateListener {
        void onStockUpdated();
        void onStockUpdateFailed(String errorMessage);
    }


    private void updateStockAfterFetch(List<ProductStock> stockList, List<ProductUpdate> updatesList, StockUpdateListener listener) {
        Log.d("DatabaseHelper", "🔄 Preparing batch stock update...");

        // ✅ Prepare a list of updates that will be sent to Supabase
        List<ProductUpdate> validUpdates = new ArrayList<>();

        for (ProductUpdate update : updatesList) {
            Optional<ProductStock> matchingStock = stockList.stream()
                    .filter(stock -> stock.getProductId().equals(update.getProductId()) &&
                            stock.getBatchNumber().equals(update.getBatchNumber()))
                    .findFirst();

            if (matchingStock.isPresent()) {
                int currentStock = matchingStock.get().getQuantity();
                int newQuantity = currentStock - update.getNewQuantity();

                if (newQuantity >= 0) {
                    // ✅ Store the valid update
                    validUpdates.add(new ProductUpdate(update.getProductId(), update.getBatchNumber(), newQuantity));

                    Log.d("DatabaseHelper", "✅ Updated Product ID: " + update.getProductId() +
                            " | Batch: " + update.getBatchNumber() + " | New Quantity: " + newQuantity);
                } else {
                    Log.e("DatabaseHelper", "❌ Not enough stock for Product ID: " + update.getProductId());
                    listener.onStockUpdateFailed("⚠️ Not enough stock for " + update.getProductId());
                    return;
                }
            } else {
                Log.e("DatabaseHelper", "❌ Stock entry NOT FOUND for Product: " + update.getProductId());
                listener.onStockUpdateFailed("❌ Stock entry not found for " + update.getProductId());
                return;
            }
        }

        if (!validUpdates.isEmpty()) {
            sendBatchStockUpdate(validUpdates, listener);
        } else {
            Log.w("DatabaseHelper", "⚠️ No stock updates needed.");
        }
    }






    private void sendBatchStockUpdate(List<ProductUpdate> updatesList, StockUpdateListener listener) {
        if (updatesList.isEmpty()) {
            Log.w("DatabaseHelper", "⚠️ No stock updates to send.");
            return;
        }

        Log.d("DatabaseHelper", "📡 Sending batch stock update to Supabase...");

        // Prepare the updates list with correct quantities for each product
        List<Map<String, Object>> updates = new ArrayList<>();
        for (ProductUpdate update : updatesList) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("quantity", update.getNewQuantity()); // Correct quantity for each product
            updates.add(updateData);
        }

        // Now, send each update separately (but still in one function)
        for (int i = 0; i < updatesList.size(); i++) {
            ProductUpdate update = updatesList.get(i);
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("product_id", "eq." + update.getProductId());
            queryMap.put("batch_number", "eq." + update.getBatchNumber());


            // Send the request for each product individually
            int finalI = i;
            supabaseApi.updateQuantities(API_KEY, AUTH_TOKEN, CONTENT_TYPE, queryMap, Collections.singletonList(updates.get(i)))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d("DatabaseHelper", "✅ Stock update successful for product ID: " + update.getProductId());
                                if (finalI == updatesList.size() - 1) {
                                    listener.onStockUpdated();  // Call onStockUpdated once after all updates
                                }
                            } else {
                                String errorBody = "";
                                try {
                                    errorBody = response.errorBody().string();
                                } catch (IOException e) {
                                    errorBody = "Unknown error";
                                }
                                Log.e("DatabaseHelper", "❌ Failed to update stock for product ID: " + update.getProductId() + " | HTTP " + response.code() + " | " + errorBody);
                                listener.onStockUpdateFailed("❌ Failed to update stock for product ID: " + update.getProductId() + " " + errorBody);
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("DatabaseHelper", "❌ Error updating stock for product ID: " + update.getProductId() + " | " + t.getMessage(), t);
                            listener.onStockUpdateFailed("❌ Error updating stock for product ID: " + update.getProductId());
                        }
                    });
        }

    }

    public void saveSalesReportAndTransactionHistory(
            String transactionNumber,
            float subtotal,
            float discount,
            float totalAmount,
            float change,
            String dateTime,
            String paymentMethod,
            float cash,
            String referenceNumber,
            String customername,
            List<Map<String, Object>> salesReportList,
            SalesSaveListener listener) {

        // First, create the transaction history map
        Map<String, Object> TransactionHistory = new HashMap<>();
        TransactionHistory.put("transaction_number", transactionNumber);
        TransactionHistory.put("subtotal", subtotal);
        TransactionHistory.put("discount", discount);
        TransactionHistory.put("total_amount", totalAmount);
        TransactionHistory.put("change", change);
        TransactionHistory.put("date", dateTime);
        TransactionHistory.put("payment_method", paymentMethod);
        TransactionHistory.put("cash", cash);
        TransactionHistory.put("reference_number", referenceNumber);
        TransactionHistory.put("customer_name", customername);

        // Save the transaction history to Supabase
        supabaseApi.insertTransactionHistory(API_KEY, AUTH_TOKEN, CONTENT_TYPE, TransactionHistory)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("DatabaseHelper", "✅ Transaction History saved successfully!");

                            // Once the transaction history is saved, proceed to save sales report
                            saveSalesReport(transactionNumber, salesReportList, listener);
                        } else {
                            String errorBody = "";
                            try {
                                errorBody = response.errorBody().string();
                            } catch (IOException e) {
                                errorBody = "Unknown error";
                            }
                            Log.e("DatabaseHelper", "❌ Failed to save transaction history: " + errorBody);
                            listener.onSaveFailed("❌ Failed to save transaction history: " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("DatabaseHelper", "❌ Error saving transaction history: " + t.getMessage(), t);
                        listener.onSaveFailed("❌ Error saving transaction history.");
                    }
                });
    }
    public interface SalesSaveListener {
        void onSaveSuccess();
        void onSaveFailed(String errorMessage);
    }
    private void saveSalesReport(String transactionNumber, List<Map<String, Object>> salesReportList, SalesSaveListener listener) {
        // Save the sales report to Supabase in batch
        supabaseApi.insertSalesReport(API_KEY, AUTH_TOKEN, CONTENT_TYPE, salesReportList)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("DatabaseHelper", "✅ Sales report inserted successfully.");
                            if (listener != null) listener.onSaveSuccess();
                        } else {
                            Log.e("DatabaseHelper", "❌ Failed to insert sales report: " + response.code());
                            if (listener != null) listener.onSaveFailed("Failed to insert sales report.");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("DatabaseHelper", "❌ Error inserting sales report: " + t.getMessage());
                        if (listener != null) listener.onSaveFailed("Error inserting sales report.");
                    }
                });

    }

    public interface ReturnInsertedListener {
        void onSaveSuccess();
        void onSaveFailed(String errorMessage);
    }

    public void insertReturnPurchase(String transactionnum,String productid,String batchnumber,String productname,String supplier,String quantity, String reason,String date, ReturnInsertedListener listener) {
        Map<String, Object> returnpurchase = new HashMap<>();
        returnpurchase.put("transaction_number", transactionnum);
        returnpurchase.put("product_id", productid);
        returnpurchase.put("batch_number", batchnumber);
        returnpurchase.put("product_name", productname);
        returnpurchase.put("supplier", supplier);
        returnpurchase.put("quantity", quantity);
        returnpurchase.put("return_reason", reason);
        returnpurchase.put("return_date", date);

        supabaseApi.insertReturnPurchase(returnpurchase).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listener.onSaveSuccess();
                    Toast.makeText(context, "Return Purchase Inserted Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    listener.onSaveFailed("Failed to insert return purchase.");
                    Toast.makeText(context, "Insert Failed: " + response.errorBody(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void getFilteredReturns(List<Map<String, String>> keys, final ReturnListCallback callback) {
        Map<String, String> queryMap = new HashMap<>();

        if (keys.size() == 1) {
            Map<String, String> key = keys.get(0);
            queryMap.put("transaction_number", "eq." + key.get("transactionNumber"));
            queryMap.put("product_id", "eq." + key.get("productId"));
            queryMap.put("batch_number", "eq." + key.get("batchNumber"));
        } else {
            List<String> orConditions = new ArrayList<>();
            for (Map<String, String> key : keys) {
                orConditions.add("and(transaction_number.eq." + key.get("transactionNumber")
                        + ",product_id.eq." + key.get("productId")
                        + ",batch_number.eq." + key.get("batchNumber") + ")");
            }
            queryMap.put("or", "(" + String.join(",", orConditions) + ")");
        }

        supabaseApi.getFilteredReturns(API_KEY, AUTH_TOKEN, CONTENT_TYPE, queryMap)
                .enqueue(new Callback<List<ReturnItem>>() {
                    @Override
                    public void onResponse(Call<List<ReturnItem>> call, Response<List<ReturnItem>> response) {
                        if (response.isSuccessful()) {
                            List<ReturnItem> returnList = response.body();
                            for (ReturnItem ret : returnList) {
                                Log.d("API Success", "Transaction Number: " + ret.getTransactionNumber()
                                        + ", Product ID: " + ret.getProductId()
                                        + ", Batch Number: " + ret.getBatchNumber());
                            }
                            callback.onSuccess(returnList);
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("Error fetching filtered returns", "Response Code: " + response.code() + " Body: " + errorBody);
                            } catch (IOException e) {
                                Log.e("Error", "Failed to read error body: " + e.getMessage());
                            }
                            callback.onError("Error fetching filtered returns");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ReturnItem>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }



    public void getFilteredTransactionHistory(Set<String> transactionNumbers, final TransactionHistoryCallback callback) {
        List<String> formatted = new ArrayList<>();
        for (String number : transactionNumbers) {
            formatted.add(number); // no need to wrap in quotes for numeric fields
        }

        String joined = "in.(" + TextUtils.join(",", formatted) + ")";
        Log.d("SupabaseQuery", "Filter: transaction_number=" + joined); // ✅ for debug

        supabaseApi.getFilteredTransactionHistory(joined, API_KEY, AUTH_TOKEN, CONTENT_TYPE)
                .enqueue(new Callback<List<TransacHis>>() {
                    @Override
                    public void onResponse(Call<List<TransacHis>> call, Response<List<TransacHis>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            String errorBody = "";
                            try {
                                errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            } catch (IOException e) {
                                errorBody = "Error reading error body: " + e.getMessage();
                            }
                            Log.e("Error fetching transaction history", "Code: " + response.code() + " Body: " + errorBody);
                            callback.onError("Error fetching transaction history: " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TransacHis>> call, Throwable t) {
                        Log.e("Error fetching transaction history", "Network failure: " + t.getMessage());
                        callback.onError(t.getMessage());
                    }
                });
    }

}
