package com.example.emjiposinv;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private SupabaseAuthApi supabaseAuthApi;



    private Button btnSale, btnCusTran, btnAvailProd, btnSup;

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIs" +
            "InJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA" +
            "1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdX" +
            "BhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczO" +
            "TA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";
    private static final String CONTENT_TYPE = "application/json";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        btnSale = view.findViewById(R.id.btnsales);
        btnCusTran = view.findViewById(R.id.btncustran);
        btnAvailProd = view.findViewById(R.id.btnavailproducts);
        btnSup = view.findViewById(R.id.btnsup);

        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);


        loadMonthlySalesOnlyMonth();
        loadCustomerTransactionsToday();
        countAvailableProducts();
        countSuppliers();

        return view;
    }

    private void loadMonthlySalesOnlyMonth() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        String likeMonth = "like." + currentMonth + "%";

        // Log the current month to check if the correct month is being used
        Log.d("Current Month", "Month: " + currentMonth);

        // Fetch sales data
        Call<List<SalesRep>> callSales = supabaseAuthApi.getSalesReportsByMonth(
                likeMonth, "*", API_KEY, AUTH_TOKEN, CONTENT_TYPE
        );

        callSales.enqueue(new Callback<List<SalesRep>>() {
            @Override
            public void onResponse(Call<List<SalesRep>> call, Response<List<SalesRep>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SalesRep> salesList = response.body();

                    Set<String> salesKeys = new HashSet<>();
                    List<Map<String, String>> returnKeys = new ArrayList<>();
                    double[] grossTotal = {0.0};
                    double[] totalReturn = {0.0}; // Make totalReturn an array

                    // Collect sales keys and calculate gross total
                    for (SalesRep sale : salesList) {
                        salesKeys.add(sale.getNormalizedKey());

                        Map<String, String> keyMap = new HashMap<>();
                        keyMap.put("transactionNumber", sale.getTransactionNumber());
                        keyMap.put("productId", sale.getProductId());
                        keyMap.put("batchNumber", sale.getBatchNumber());
                        returnKeys.add(keyMap);

                        try {
                            grossTotal[0] += sale.getTotalPrice();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Log.d("Gross Total", "Gross Total: " + grossTotal[0]);

                    // Fetch filtered returns and calculate total return
                    getFilteredReturns(returnKeys, new NewReturnListCallback() {
                        @Override
                        public void onSuccess(List<ReturnTB> returnList) {
                            // Loop through returns and calculate total return for matching keys
                            for (ReturnTB ret : returnList) {
                                String returnKey = ret.getNormalizedKey();
                                Log.d("Return Key", "Checking Return Key: " + returnKey);

                                if (salesKeys.contains(returnKey)) {
                                    try {
                                        // Find matching sale
                                        SalesRep matchedSale = findMatchingSale(salesList, returnKey);
                                        if (matchedSale != null) {
                                            double unitPrice = matchedSale.getUnitPrice();
                                            double returnQuantity = ret.getQuantity(); // No need to parse as String

                                            Log.d("Unit Price", "Unit Price: " + unitPrice);
                                            Log.d("Return Quantity", "Return Quantity: " + returnQuantity);

                                            // Only add return if quantity is valid
                                            if (returnQuantity > 0) {
                                                totalReturn[0] += unitPrice * returnQuantity; // Update totalReturn inside the callback
                                            } else {
                                                Log.d("Return Issue", "Invalid return quantity for key: " + returnKey);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            Log.d("Total Return", "Total Return: " + totalReturn[0]);

                            // Get total discount from TransactionHistoryTB
                            // Collect transaction numbers from salesList
                            List<String> transactionNumbers = new ArrayList<>();
                            for (SalesRep sale : salesList) {
                                transactionNumbers.add(sale.getTransactionNumber());
                            }

                            // Fetch transaction history using the collected transaction numbers
                            getTransactionHistoryForMonth(transactionNumbers, new TransactionHistoryCallback() {
                                @Override
                                public void onSuccess(List<TransactionHistoryTB> historyList) {
                                    double totalDiscount = 0.0;

                                    // Loop through history list and sum up the discounts
                                    for (TransactionHistoryTB history : historyList) {
                                        double discount = history.getDiscount();
                                        totalDiscount += discount;
                                    }

                                    Log.d("Total Discount", "Total Discount: " + totalDiscount);

                                    // Calculate Net Sale (Gross Total - Total Return - Total Discount)
                                    double netSale = grossTotal[0] - totalReturn[0] - totalDiscount;
                                    Log.d("Net Sale After", "Net Sale: " + netSale);

                                    // Update UI with calculated Net Sale
                                    NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
                                    btnSale.setText("SALES THIS MONTH:\n" + pesoFormat.format(netSale));
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Log.e("Error", "Failed to fetch transaction history: " + errorMessage);
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("Error", "Failed to fetch filtered returns: " + errorMessage);
                        }
                    });
                } else {
                    Log.e("Error", "Response was not successful or body was null.");
                }
            }

            @Override
            public void onFailure(Call<List<SalesRep>> call, Throwable t) {
                t.printStackTrace();
                Log.e("API Error", "Failed to fetch sales reports: " + t.getMessage());
            }
        });
    }

    // Fetch transaction history for the month to calculate total discount
    private void getTransactionHistoryForMonth(List<String> transactionNumbers, TransactionHistoryCallback callback) {
        // Create the "or" condition for the list of transaction numbers with quoted values
        List<String> orConditions = new ArrayList<>();
        for (String transactionNumber : transactionNumbers) {
            orConditions.add("transaction_number.eq." + "\"" + transactionNumber + "\"");
        }

        // Construct the query map with the properly formatted "or" condition
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("or", "(" + String.join(",", orConditions) + ")");
        queryMap.put("select", "*");

        // Call Supabase API
        Call<List<TransactionHistoryTB>> callHistory = supabaseAuthApi.getTransactionHistoryFilteredByTransactionNumbers(
                queryMap, API_KEY,  AUTH_TOKEN, CONTENT_TYPE
        );

        callHistory.enqueue(new Callback<List<TransactionHistoryTB>>() {
            @Override
            public void onResponse(Call<List<TransactionHistoryTB>> call, Response<List<TransactionHistoryTB>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError("No data returned from the server.");
                        Log.e("API Error", "Response body is null.");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        callback.onError("Failed to fetch transaction history: " + errorBody);
                        Log.e("API Error", "Failed to fetch transaction history: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.onError("Failed to parse error response.");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TransactionHistoryTB>> call, Throwable t) {
                t.printStackTrace();
                callback.onError("Error fetching transaction history: " + t.getMessage());
                Log.e("API Error", "Error fetching transaction history: " + t.getMessage());
            }
        });
    }

    // Callback interface for transaction history
    public interface TransactionHistoryCallback {
        void onSuccess(List<TransactionHistoryTB> historyList);
        void onError(String errorMessage);
    }

    // Find matching sale based on normalized keys
    public SalesRep findMatchingSale(List<SalesRep> salesList, String normalizedKey) {
        for (SalesRep sale : salesList) {
            if (sale.getNormalizedKey().equals(normalizedKey)) {
                return sale;
            }
        }
        return null;
    }

    public void getFilteredReturns(List<Map<String, String>> keys, final NewReturnListCallback callback) {
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

        supabaseAuthApi.getFilteredReturnsDashboard(API_KEY, AUTH_TOKEN, CONTENT_TYPE, queryMap)
                .enqueue(new Callback<List<ReturnTB>>() {
                    @Override
                    public void onResponse(Call<List<ReturnTB>> call, Response<List<ReturnTB>> response) {
                        if (response.isSuccessful()) {
                            List<ReturnTB> returnList = response.body();
                            for (ReturnTB ret : returnList) {
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
                    public void onFailure(Call<List<ReturnTB>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }
    private void loadCustomerTransactionsToday() {
        // Step 1: Get today's date in yyyy-MM-dd format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String dateFilter = "like." + todayDate + "%"; // If your date includes time (timestamp)

        // Step 2: Make API call to Supabase
        Call<List<TransactionHistoryTB>> call = supabaseAuthApi.getTransactionsForDate(
                dateFilter,
                "*", // Select all fields or just "transaction_number" if you only need count
                API_KEY,
                AUTH_TOKEN,
                CONTENT_TYPE
        );

        // Step 3: Handle API response
        call.enqueue(new Callback<List<TransactionHistoryTB>>() {
            @Override
            public void onResponse(Call<List<TransactionHistoryTB>> call, Response<List<TransactionHistoryTB>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int transactionCount = response.body().size();

                    // ✅ Display the count to your button
                    btnCusTran.setText("CUSTOMER TRANSACTIONS TODAY: \n" + transactionCount);

                    // Optional Log for debugging
                    Log.d("Customer Transactions", "Count: " + transactionCount);
                } else {
                    Log.e("Error", "Response failed: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("Error Body", response.errorBody().string());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    btnCusTran.setText("CUSTOMER TRANSACTIONS TODAY:\n0"); // fallback
                }
            }

            @Override
            public void onFailure(Call<List<TransactionHistoryTB>> call, Throwable t) {
                Log.e("API Failure", "Error: " + t.getMessage());
                btnCusTran.setText("CUSTOMER TRANSACTIONS TODAY:\n0"); // fallback on error
            }
        });
    }

    private void countAvailableProducts() {
        // DO NOT manually add "or=" here. Let Retrofit handle it via @Query("or")
        String statusFilter = "(status.eq.In Stock,status.eq.Re Stock)";

        Call<List<Product>> call = supabaseAuthApi.getAvailableProducts(
                statusFilter,
                "*",
                API_KEY,
                AUTH_TOKEN,
                CONTENT_TYPE
        );

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int availableCount = response.body().size();
                    btnAvailProd.setText("AVAILABLE PRODUCTS: \n" + availableCount);
                } else {
                    Log.e("AvailableProdError", "Failed: " + response.code());
                    Log.e("AvailableProdErrorBody", "Body: " + response.message());
                    btnAvailProd.setText("AVAILABLE PRODUCTS: \n0");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("API Error", "Request failed: " + t.getMessage());
                btnAvailProd.setText("AVAILABLE PRODUCTS: \n0");
            }
        });
    }


    private void countSuppliers() {
        Log.d("API", "Fetching suppliers...");

        Call<List<Supplier>> call = supabaseAuthApi.getAllSuppliers(
                API_KEY,
                AUTH_TOKEN,
                CONTENT_TYPE
        );

        call.enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int supplierCount = response.body().size();
                    btnSup.setText(String.format(Locale.getDefault(), "SUPPLIERS: %d", supplierCount));
                } else {
                    Log.e("SupplierCountError", "Response was not successful or body was null. " +
                            "Response Code: " + response.code() + ", Response Body: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                Log.e("SupplierCountError", "Failed to fetch suppliers: " + t.getMessage());
            }
        });
    }








}
