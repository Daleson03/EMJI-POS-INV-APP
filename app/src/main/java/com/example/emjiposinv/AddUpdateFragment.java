package com.example.emjiposinv;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUpdateFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private EditText productId, productName, unitPrice, purchasePrice;
    private Spinner supplierSpinner, statusSpinner, batchSpinner;
    private Button scanButton, insertButton, updateButton, searchButton;
    private TextView txtbatch;
    private List<String> supplierList = new ArrayList<>();

    private List<String> BatchList = new ArrayList<>();
    private SupabaseAuthApi supabaseAuthApi;
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xa" +
            "HFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt" +
            "2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";

    // ✅ Barcode Scanner Launcher
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    productId.setText(scannedCode);

                    // ✅ Fetch Batch Numbers Based on Product ID
                    fetchBatchtoSpinner(scannedCode);

                    // ✅ Check if the product exists in Supabase
                    databaseHelper.getProductByBarcode(scannedCode, new ProductCallback() {
                        @Override
                        public void onProductFound(Product product) {
                            productName.setText(product.getProductName());

                            showViews(batchSpinner,txtbatch);

                            // ✅ Set Spinner selections
                            setSpinnerSelection(supplierSpinner, product.getSupplier());
                            setSpinnerSelection(statusSpinner, product.getStatus());

                            updateButton.setEnabled(true);
                            insertButton.setEnabled(false);
                        }

                        @Override
                        public void onProductNotFound() {
                            productName.setText("");
                            unitPrice.setText("");
                            purchasePrice.setText("");

                            hideViews(batchSpinner,txtbatch);

                            supplierSpinner.setSelection(0);
                            statusSpinner.setSelection(0);
                            clearBatchSpinner();
                            updateButton.setEnabled(false);
                            insertButton.setEnabled(true);
                            Toast.makeText(getContext(), "Product not found, please add details.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_update, container, false);

        // ✅ Initialize Database
        databaseHelper = new DatabaseHelper(requireContext());

        // ✅ Initialize UI Components
        productId = view.findViewById(R.id.txtproductid);
        productName = view.findViewById(R.id.txtproductname);
        unitPrice = view.findViewById(R.id.txtunitprice);
        purchasePrice = view.findViewById(R.id.txtpurchaseprice);
        supplierSpinner = view.findViewById(R.id.ddsupplierinstock);
        statusSpinner = view.findViewById(R.id.ddstatusinstock);
        batchSpinner = view.findViewById(R.id.ddbatchnumber);
        scanButton = view.findViewById(R.id.btnscan);
        insertButton = view.findViewById(R.id.btnConnectPrinter);
        updateButton = view.findViewById(R.id.btnupload);
        searchButton = view.findViewById(R.id.btnsearch2);
        txtbatch = view.findViewById(R.id.textView11);


        // Initially hide all related views
        hideViews(batchSpinner,txtbatch);


        // ✅ Setup Dropdowns (Spinners)
        setupSpinner(statusSpinner, getStatusList());

        // ✅ Set Button Actions
        scanButton.setOnClickListener(v -> startBarcodeScanner());
        insertButton.setOnClickListener(v -> insertProduct());
        updateButton.setOnClickListener(v -> updateProduct());



        searchButton.setOnClickListener(v -> {
            String productid = productId.getText().toString();

            // ✅ Fetch Batch Numbers Based on Product ID
            fetchBatchtoSpinner(productid);

            // ✅ Check if the product exists in Supabase
            databaseHelper.getProductByBarcode(productid, new ProductCallback() {
                @Override
                public void onProductFound(Product product) {
                    productName.setText(product.getProductName());

                    showViews(batchSpinner,txtbatch);

                    // ✅ Set Spinner selections
                    setSpinnerSelection(supplierSpinner, product.getSupplier());
                    setSpinnerSelection(statusSpinner, product.getStatus());

                    updateButton.setEnabled(true);
                    insertButton.setEnabled(false);
                }

                @Override
                public void onProductNotFound() {
                    productName.setText("");
                    unitPrice.setText("");
                    purchasePrice.setText("");

                    hideViews(batchSpinner,txtbatch);

                    supplierSpinner.setSelection(0);
                    statusSpinner.setSelection(0);
                    clearBatchSpinner();
                    updateButton.setEnabled(false);
                    insertButton.setEnabled(true);
                    Toast.makeText(getContext(), "Product not found, please add details.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        updateButton.setEnabled(false);

        // ✅ Fetch suppliers from Supabase
        fetchSupplierstoSpinner();

        batchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBatch = batchSpinner.getSelectedItem().toString();
                String selectedProductId = productId.getText().toString();

                if (!selectedBatch.equals("Select Batch") && !selectedProductId.isEmpty()) {
                    fetchProductByBatch(selectedProductId, selectedBatch);
                }
                unitPrice.setText("");
                purchasePrice.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }


    // Helper function to hide multiple views
    private void hideViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    // Helper function to show multiple views
    private void showViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }


    private void fetchProductByBatch(String productId, String batchNumber) {
        supabaseAuthApi.getProductByBatch("eq." + productId, "eq." + batchNumber, API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Product product = response.body().get(0); // Assuming only one match

                    productName.setText(product.getProductName());
                    unitPrice.setText(String.valueOf(product.getUnitPrice()));
                    purchasePrice.setText(String.valueOf(product.getPurchasePrice()));

                    setSpinnerSelection(supplierSpinner, product.getSupplier());
                    setSpinnerSelection(statusSpinner, product.getStatus());

                    Toast.makeText(getContext(), "Product data loaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No data found for this batch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ✅ Fetch Supplier Names from Supabase
    private void fetchSupplierstoSpinner() {
        supabaseAuthApi.getSupplierstoSpinner(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    supplierList.clear();
                    supplierList.add("Select Supplier"); // ✅ Add default option

                    for (Supplier supplier : response.body()) {
                        supplierList.add(supplier.getSupplierName());
                    }
                    populateSpinner();
                } else {
                    Log.e("Supabase", "Failed to fetch suppliers: " + response.code() + " - " + response.message());
                    Toast.makeText(requireContext(), "Failed to fetch suppliers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Populate Supplier Spinner
    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, supplierList);
        supplierSpinner.setAdapter(adapter);
    }

    private void fetchBatchtoSpinner(String productId) {
        String statusFilter = "(status.eq.In Stock,status.eq.Re Stock)"; // Correct filtering format

        supabaseAuthApi.getBatchtoSpinner("eq." + productId, statusFilter, API_KEY, AUTH_TOKEN)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            BatchList.clear();
                            BatchList.add("Select Batch"); // ✅ Default option

                            for (Product product : response.body()) {
                                BatchList.add(product.getBatchNumber());
                            }
                            populateSpinnerBatch();
                        } else {
                            Toast.makeText(requireContext(), "No batches found for this product.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // ✅ Populate Supplier Spinner
    private void populateSpinnerBatch() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, BatchList);
        batchSpinner.setAdapter(adapter);
    }
    // ✅ Setup Dropdowns
    private void setupSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
    }



    private List<String> getStatusList() {
        List<String> items = new ArrayList<>();
        items.add("In Stock");
        items.add("Re Stock");
        items.add("Out of Stock");
        return items;
    }

    // Start Barcode Scanner
    private void startBarcodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt("Scan a barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(MyCaptureActivity.class);

        barcodeLauncher.launch(options);
    }

    // Insert Product into Database
    private void insertProduct() {
        String name = productName.getText().toString();
        String supplierName = supplierSpinner.getSelectedItem().toString();
        String productStatus = statusSpinner.getSelectedItem().toString();



        if (name.isEmpty() || productId.getText().toString().isEmpty() || supplierName.isEmpty() ||
                unitPrice.getText().toString().isEmpty() || purchasePrice.getText().toString().isEmpty() || productStatus.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (supplierName.equals("Select Supplier")) {
            Toast.makeText(getContext(), "Please select a valid supplier!", Toast.LENGTH_SHORT).show();
            return;
        }

        String productid = productId.getText().toString();
        double price = Double.parseDouble(unitPrice.getText().toString());
        double purchase = Double.parseDouble(purchasePrice.getText().toString());

        String unit = "Pck";
        String batchNumber = "";
        int quantity = 0;
        String expirationDate = null;


        databaseHelper.insertProduct( batchNumber, name, supplierName, unit, productid, quantity, price, purchase, expirationDate, productStatus);

        clearFields();
        Toast.makeText(getContext(), "Product Adding...", Toast.LENGTH_SHORT).show();
    }

    // ✅ Update Existing Product
    private void updateProduct() {
        String name = productName.getText().toString();
        String supplierName = supplierSpinner.getSelectedItem().toString();
        String productStatus = statusSpinner.getSelectedItem().toString();
        String batch = batchSpinner.getSelectedItem().toString();



        if (name.isEmpty() || productId.getText().toString().isEmpty() || supplierName.isEmpty() ||
                unitPrice.getText().toString().isEmpty() || purchasePrice.getText().toString().isEmpty() || productStatus.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (supplierName.equals("Select Supplier")) {
            Toast.makeText(getContext(), "Please select a valid supplier!", Toast.LENGTH_SHORT).show();
            return;
        }

        String productid = productId.getText().toString();
        double price = Double.parseDouble(unitPrice.getText().toString());
        double purchase = Double.parseDouble(purchasePrice.getText().toString());

        if (batch.equalsIgnoreCase("Select Batch")) {

            databaseHelper.updateProduct(productid, name, supplierName, price, purchase, productStatus);

            clearFields();
            Toast.makeText(getContext(), "Product Updating...", Toast.LENGTH_SHORT).show();

        } else {
            databaseHelper.updateProductwithBatch(productid, batch, name, supplierName, price, purchase, productStatus);

            clearFields();
            Toast.makeText(getContext(), "Product Updating...", Toast.LENGTH_SHORT).show();

        }
    }

    // Helper to Set Spinner Selection
    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }
    private void clearBatchSpinner() {
        BatchList.clear();
        BatchList.add("Select Batch");
        populateSpinnerBatch();
    }
    // Clear Fields
    private void clearFields() {
        productId.setText("");
        productName.setText("");
        unitPrice.setText("");
        purchasePrice.setText("");
        clearBatchSpinner();
        supplierSpinner.setSelection(0);
        statusSpinner.setSelection(0);
        updateButton.setEnabled(false);
    }
}
