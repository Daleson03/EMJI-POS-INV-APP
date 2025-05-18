package com.example.emjiposinv;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.List;
import java.util.Map;

public interface SupabaseAuthApi {
    @Headers({
            "Content-Type: application/json",
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00"
    })
    @POST("auth/v1/signup")
        // ✅ Correct Supabase sign-up endpoint
    Call<Map<String, Object>> signUp(@Body Map<String, String> user);

    @Headers({
            "Content-Type: application/json",
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00"
    })
    @POST("auth/v1/token?grant_type=password")
        // ✅ Fix login endpoint
    Call<Map<String, Object>> signIn(@Body Map<String, String> user);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json"
    })
    @GET("rest/v1/AccountTB")
    Call<List<Map<String, Object>>> getAccountByEmail(
            @Query("email") String emailEq // Still `email`, but pass "eq.email@example.com"
    );
    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json"
    })
    @POST("rest/v1/ProductListTB")
    Call<Void> insertProduct(@Body Map<String, Object> product);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json"
    })
    @POST("rest/v1/SupplierTB")
    Call<Void> insertSupplier(@Body Map<String, Object> supplier);



    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json",
            "Prefer: return=representation" // ✅ Ensures updated data is returned
    })
    @PATCH("rest/v1/ProductListTB")
    Call<Void> updateProduct(
            @Query("product_id") String filter,  // ✅ Correct filter
            @Body Map<String, Object> updatedProduct
    );

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json",
            "Prefer: return=representation" // ✅ Ensures updated data is returned
    })
    @PATCH("rest/v1/ProductListTB")
    Call<Void> updateProductStock(
            @Query("product_id") String filter,  // ✅ Correct filter
            @Body Map<String, Object> updatedProduct
    );
    @POST("rest/v1/StockHistoryTB")
    Call<Void> insertStockHistory(
            @Body Map<String, Object> historyData,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );
    @GET("rest/v1/ProductListTB")
    Call<List<Map<String, Object>>> getBatchByProduct(
            @Query("product_id") String filterProductId,
            @Query("batch_number") String filterBatchNum,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/ProductListTB")
    Call<List<Map<String, Object>>> getProductById(
            @Query("product_id") String filterProductId,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @PATCH("rest/v1/ProductListTB")
    Call<Void> updateProductStockwithBatch(
            @Query("product_id") String filterProductId,
            @Query("batch_number") String filterBatchNum,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body Map<String, Object> updatedStock
    );

    @POST("rest/v1/ProductListTB")
    Call<Void> insertNewStock(
            @Body Map<String, Object> newStock,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );


    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json",
            "Prefer: return=representation" // ✅ Ensures updated data is returned
    })
    @PATCH("rest/v1/ProductListTB")
    Call<Void> updateProductwithbatch(
            @Query("product_id") String productidfilter,  // ✅ Correct filter
            @Query("batch_number") String batchnumberfilter,  // ✅ Correct filter
            @Body Map<String, Object> updatedProduct
    );
    @GET("rest/v1/ProductListTB")
    Call<List<Product>> getProductByBatch(
            @Query("product_id") String productId,
            @Query("batch_number") String batchNumber,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken

    );
    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json",
            "Prefer: return=representation" // ✅ Ensures updated data is returned
    })
    @PATCH("rest/v1/SupplierTB")
    Call<Void> updateSupplier(@Query("SupplierID") String filter, @Body Map<String, Object> updateSupplier);

    @GET("rest/v1/ProductListTB")
        // ✅ Ensure correct path
    Call<List<Product>> getProductByBarcode(
            @Query("product_id") String productId,  // ✅ Ensure correct filter format
            @Query("select") String selectFields,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/TransactionHistoryTB")
        // ✅ Ensure correct path
    Call<List<TransactionHistory>> getTransactionByBarcode(
            @Query("transaction_number") String transactionnum,  // ✅ Ensure correct filter format
            @Query("select") String selectFields,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/ProductListTB")
    Call<List<Product>> getAllProducts(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/SalesReportTB")
    Call<List<SalesReport>> getAllSalesReport(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/TransactionHistoryTB")
    Call<List<TransactionHistory>> getAllTransactionHistory(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/ReturnTB")
    Call<List<ReturnPurchase>> getAllReturnPurchase(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/StockHistoryTB")
    Call<List<Stock>> getAllStockHistory(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/SupplierTB?select=*&order=SupplierID.asc") // Table name in Supabase
    Call<List<Supplier>> getAllSupplier(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );


    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json"
    })

    @GET("rest/v1/SupplierTB?select=*&order=SupplierID.asc,SupplierName,ContactPerson,Phone,Email,CutoffTime")
    Call<List<Supplier>> getSuppliers();


    @GET("rest/v1/SupplierTB") // Table name in Supabase
    Call<List<Supplier>> getSupplierstoSpinner(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/ProductListTB")
    Call<List<Product>> getBatchtoSpinner(
            @Query("product_id") String productIdFilter,
            @Query("or") String statusFilter, // Combine conditions here
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/SalesReportTB")
    Call<List<SalesReport>> getProductfromTransactiontoSpinner(
            @Query("transaction_number") String transactionnum,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/SalesReportTB")
    Call<List<SalesReport>> getProductData(
            @Query("transaction_number") String transacnum,
            @Query("product_id") String productId,
            @Query("batch_number") String batchNumber,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken

    );


    @GET("rest/v1/SupplierTB") // Table name in Supabase
    Call<List<Supplier>> getSupplierstoSpinnerinprodlist(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );


    // ✅ Fetch stock for multiple products using filtering
    @GET("rest/v1/ProductListTB")
    Call<List<ProductStock>> getStock(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType, // ✅ Ensure Content-Type header is included
            @QueryMap Map<String, String> filters // ✅ Use QueryMap for flexible filters
    );

    @PATCH("rest/v1/ProductListTB")
    Call<Void> updateQuantities(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @QueryMap Map<String, String> queryParams, // Pass query conditions dynamically
            @Body List<Map<String, Object>> updates // ✅ Updated to accept List<Map<String, Object>>
    );


    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json"
    })
    @POST("rest/v1/AccountTB")
    Call<Void> insertaccount(@Body Map<String, Object> account);


    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json"
    })
    @POST("rest/v1/AccountLogTB")
    Call<Void> insertAccountLog(@Body Map<String, Object> logEntry);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json",
            "Prefer: return=representation" // ✅ Ensures updated data is returned
    })
    @PATCH("rest/v1/AccountTB")
    Call<Void> updateaccount(
            @Query("email") String filter,  // ✅ Correct filter
            @Body Map<String, Object> updateaccount
    );

    @POST("rest/v1/TransactionHistoryTB")
    Call<Void> insertTransactionHistory(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @Body Map<String, Object> TransactionHistory
    );

    @POST("rest/v1/SalesReportTB")
    Call<Void> insertSalesReport(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @Body List<Map<String, Object>> salesReportList
    );

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA",
            "Content-Type: application/json"
    })
    @POST("rest/v1/ReturnTB")
    Call<Void> insertReturnPurchase(@Body Map<String, Object> returnpurchase);

    @GET("rest/v1/ReturnTB")
    Call<List<ReturnItem>> getFilteredReturns(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @QueryMap Map<String, String> filters
    );

    @GET("rest/v1/TransactionHistoryTB")
    Call<List<TransacHis>> getFilteredTransactionHistory(
            @Query("transaction_number") String inClause,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType
    );


    // Get sales reports filtered by current month
    @GET("rest/v1/SalesReportTB")
    Call<List<SalesRep>> getSalesReportsByMonth(
            @Query("date") String likeMonth,
            @Query("select") String select,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType
    );

    @GET("rest/v1/ReturnTB")
    Call<List<ReturnTB>> getFilteredReturnsDashboard(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @QueryMap Map<String, String> queryMap
    );

    @GET("rest/v1/TransactionHistoryTB")
    Call<List<TransactionHistoryTB>> getTransactionHistoryFilteredByTransactionNumbers(
            @QueryMap Map<String, String> queryMap,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType
    );

    @GET("rest/v1/TransactionHistoryTB")
    Call<List<TransactionHistoryTB>> getTransactionsForDate(
            @Query("date") String dateQuery,
            @Query("select") String select,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType
    );

    @GET("rest/v1/ProductListTB")
    Call<List<Product>> getAvailableProducts(
            @Query("or") String statusFilter,
            @Query("select") String select,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType
    );

    @GET("rest/v1/SupplierTB?select=*&limit=1000")
    Call<List<Supplier>> getAllSuppliers(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType
    );


    @GET("rest/v1/AccountTB")
    Call<List<Account>> getAllAccountData(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/AccountLogTB")
    Call<List<AccountLog>> getAllAccountLog(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @GET("rest/v1/SupplierTB")
    Call<List<SupplierNotif>> getSuppliers(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @Query("select") String select
    );

    // Fetch all products from ProductListTB
    @GET("rest/v1/ProductListTB?select=*&limit=1000")
    Call<List<ProductStatus>> getAllProducts(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType
    );

    // Update product status by product_id AND batch_number
    @PATCH("rest/v1/ProductListTB")
    Call<Void> updateProductStatus(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType,
            @Query("product_id") String productId,              // Used as product_id=eq.<value>
            @Query("batch_number") String batchNumber,          // Used as batch_number=eq.<value>
            @Body Map<String, Object> statusUpdate              // Example: { "status": "restock" }
    );

    @GET("rest/v1/ProductListTB?select=*")
    Call<List<ProductStockAlert>> getAllProductsforStockAlert(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Content-Type") String contentType
    );


    // 1. Get user by email (from Supabase Auth)
    @GET("/auth/v1/admin/users")
    Call<List<User>> getUserByEmail(
            @Header("Authorization") String authToken, // Bearer token
            @Header("apikey") String apiKey, // API key for authorization
            @Query("email") String email
    );

    // 2. Delete user from Supabase Auth (by user ID, not email)
    @DELETE("auth/v1/admin/users/{id}")
    Call<Void> deleteUserFromAuth(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Path("id") String userId
    );

    // 3. Delete user from your custom AccountTB table
    @DELETE("rest/v1/AccountTB")
    Call<Void> deleteUserFromAccountTable(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("email") String email
    );

}





