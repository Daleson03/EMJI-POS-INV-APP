package com.example.emjiposinv;

import static android.text.Selection.setSelection;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReturnPurchaseFragment extends Fragment {
    private DatabaseHelper databaseHelper;
    private String transactionnum, productid, batchnumber, productname, supplier,quantity, reason, date, selectedquantity;

    private TableLayout tableLayout;
    private SupabaseAuthApi supabaseAuthApi;
    private Spinner spinnerProduct, spinnerReason;
    private EditText txtTransactionNum, txtOtherReason, txtquantity;
    private TextView txtproductinreturn;
    private Button btnScanReturn, btnReturnPurchase;

    private List<String> ProductList = new ArrayList<>();

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIs" +
            "InJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA" +
            "1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdX" +
            "BhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczO" +
            "TA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_return_purchase, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        // ✅ Initialize Database
        databaseHelper = new DatabaseHelper(requireContext());

        spinnerProduct = view.findViewById(R.id.spinnerproduct);
        spinnerReason = view.findViewById(R.id.spinnerReason);

        txtTransactionNum = view.findViewById(R.id.txttransactionnumber);
        txtOtherReason = view.findViewById(R.id.txtOtherReason);
        txtquantity = view.findViewById(R.id.txtquantityreturn);

        txtproductinreturn = view.findViewById(R.id.txtproductinreturn);

        btnScanReturn = view.findViewById(R.id.btnreturnscan);
        btnReturnPurchase = view.findViewById(R.id.btnreturnpurchase);

        btnReturnPurchase.setEnabled(false);

        // ✅ Setup Dropdowns (Spinners)
        setupSpinner(spinnerReason, getReasonList());


        btnScanReturn.setOnClickListener(v -> startBarcodeScanner());
        btnReturnPurchase.setOnClickListener(V -> insertreturnpurchase());

        hideViews(txtOtherReason, txtproductinreturn, spinnerProduct);

        spinnerReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedReason = parent.getItemAtPosition(position).toString();

                if (selectedReason.equals("Other reason")) {
                    showViews(txtOtherReason);
                } else {
                    hideViews(txtOtherReason);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                hideViews(txtOtherReason);
            }
        });

        spinnerProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getSelectedItem().toString(); // Get the full string from the spinner
                String selectedBatch = "";         // Default value for batch number
                String selectedProductId = "";     // Default value for product id
                String transacnum = txtTransactionNum.getText().toString();

                if (!selectedItem.isEmpty()) {
                    // Assuming format: "productid-batch_number product_name Qty: quantity"
                    String[] parts = selectedItem.split(" "); // Split by space

                    if (parts.length > 2) {
                        // parts[0] should be "productid-batch_number"
                        String productBatch = parts[0];
                        String[] productBatchParts = productBatch.split("-");

                        if (productBatchParts.length == 2) {
                            selectedProductId = productBatchParts[0]; // productid
                            selectedBatch = productBatchParts[1];     // batch_number
                        }

                        // Search for "Qty:" in parts and get the number that comes after
                        for (int i = 0; i < parts.length; i++) {
                            if (parts[i].equalsIgnoreCase("Qty:") && i + 1 < parts.length) {
                                selectedquantity = parts[i + 1]; // e.g. "10"
                                break;
                            }
                        }
                    }
                }



                // Call your function if valid
                if (!selectedBatch.equals("Select Product") && !selectedProductId.isEmpty()) {
                    fetchProductData(transacnum, selectedProductId, selectedBatch);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional
            }
        });


        fetchData();

        return view;
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    txtTransactionNum.setText(scannedCode);

                    // ✅ Fetch Batch Numbers Based on Product ID
                    fetchProductfromTransactiontoSpinner(scannedCode);

                    // ✅ Check if the product exists in Supabase
                    databaseHelper.getTransactionByBarcode(scannedCode, new TransactionCallback() {
                        @Override
                        public void onTransactionFound(TransactionHistory transactionHistory) {
                            showViews(txtproductinreturn, spinnerProduct);

                            btnReturnPurchase.setEnabled(true);
                        }

                        @Override
                        public void onTransactionNotFound() {
                            hideViews(txtproductinreturn, spinnerProduct);
                            spinnerProduct.setSelection(0);
                            spinnerReason.setSelection(0);
                            txtOtherReason.setText("");
                            txtquantity.setText("");

                            clearProductSpinner();
                            btnReturnPurchase.setEnabled(false);
                            Toast.makeText(getContext(), "Transaction not found!", Toast.LENGTH_SHORT).show();
                        }

                    });
                } else {
                    Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // ✅ Start Barcode Scanner
    private void startBarcodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt("Scan transaction number barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(MyCaptureActivity.class);

        barcodeLauncher.launch(options);
    }

    private void fetchProductfromTransactiontoSpinner(String transactionnum) {

        supabaseAuthApi.getProductfromTransactiontoSpinner("eq." + transactionnum, API_KEY, AUTH_TOKEN)
                .enqueue(new Callback<List<SalesReport>>() {
                    @Override
                    public void onResponse(Call<List<SalesReport>> call, Response<List<SalesReport>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            ProductList.clear();
                            ProductList.add("Select Product"); // ✅ Default option

                            for (SalesReport salesreport : response.body()) {
                                ProductList.add(salesreport.getProductId() + "-" + salesreport.getBatchNumber() + " " + salesreport.getProductName() + " Qty: " + salesreport.getQuantity());
                            }
                            populateSpinnerProduct();
                        } else {
                            Toast.makeText(requireContext(), "No product found for this transaction number.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SalesReport>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }    // Populate Product Spinner
    private void populateSpinnerProduct() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, ProductList);
        spinnerProduct.setAdapter(adapter);
    }

    private void clearProductSpinner() {
        ProductList.clear();
        ProductList.add("Select Product");
        populateSpinnerProduct();
    }

    private void setupSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
    }

    private List<String> getReasonList() {
        List<String> items = new ArrayList<>();
        items.add("Damaged item.");
        items.add("Expired item.");
        items.add("Other reason");
        return items;
    }
    // Clear Fields
    private void clearFields() {
        transactionnum = "";
        productid = "";
        batchnumber = "";
        productname = "";
        supplier = "";
        quantity = "";
        txtquantity.setText("");
        txtOtherReason.setText("");
        spinnerReason.setSelection(0);
    }
    private void fetchProductData(String transacnum, String productId, String batchNumber) {
        supabaseAuthApi.getProductData("eq." + transacnum, "eq." + productId, "eq." + batchNumber, API_KEY, AUTH_TOKEN).enqueue(new Callback<List<SalesReport>>() {
            @Override
            public void onResponse(Call<List<SalesReport>> call, Response<List<SalesReport>> response) {
                if (!isAdded()) return; // ✅ Prevent UI updates if the fragment is not attached

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    SalesReport salesreport = response.body().get(0);
                    transactionnum = String.valueOf((salesreport.getTransactionNumber()));
                    productid = String.valueOf((salesreport.getProductId()));
                    batchnumber = String.valueOf((salesreport.getBatchNumber()));
                    productname = String.valueOf((salesreport.getProductName()));
                    supplier = String.valueOf((salesreport.getSupplier()));
                    Toast.makeText(getContext(), "Product data loaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No data found for this batch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SalesReport>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertreturnpurchase() {
        date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        quantity = txtquantity.getText().toString();
        String spinreason = spinnerReason.getSelectedItem().toString();
        String spinproduct = spinnerProduct.getSelectedItem().toString();


        if (spinreason.equals("Other reason")) {
            reason = txtOtherReason.getText().toString();
        } else {
           reason = spinnerReason.getSelectedItem().toString();
        }

        if (transactionnum.isEmpty() || productid.isEmpty() || batchnumber.isEmpty() || productname.isEmpty()
                || supplier.isEmpty() || quantity.isEmpty() || reason.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinproduct.equals("Select Product")) {
            Toast.makeText(getContext(), "Please select a product to return!", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantity);
            if (qty <= 0) {
                Toast.makeText(getContext(), "Please insert valid product quantity greater than zero!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid quantity format!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (Integer.parseInt(quantity) > Integer.parseInt(selectedquantity)) {
            Toast.makeText(getContext(), "Inserted quantity is greater than the purchase product quantity!", Toast.LENGTH_SHORT).show();
            return; // stop if input quantity is more than available
        }


        if (Integer.parseInt(quantity) > Integer.parseInt(selectedquantity)) {
            Toast.makeText(getContext(), "Inserted quantity is greater than the purchase product quantity!", Toast.LENGTH_SHORT).show();
            return;
        }

// Check existing return quantities
        int totalReturnedQuantity = 0;
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            String rowTransNum = ((TextView) row.getChildAt(0)).getText().toString();
            String rowProductId = ((TextView) row.getChildAt(1)).getText().toString();
            String rowBatchNum = ((TextView) row.getChildAt(2)).getText().toString();
            String rowQuantity = ((TextView) row.getChildAt(5)).getText().toString();

            if (rowTransNum.equals(transactionnum) && rowProductId.equals(productid) && rowBatchNum.equals(batchnumber)) {
                totalReturnedQuantity += Integer.parseInt(rowQuantity);
            }
        }

        int inputQty = Integer.parseInt(quantity);
        if (totalReturnedQuantity + inputQty > Integer.parseInt(selectedquantity)) {
            Toast.makeText(getContext(), "Total return quantity exceeds available purchased quantity!", Toast.LENGTH_SHORT).show();
            return;
        }


        databaseHelper.insertReturnPurchase(transactionnum, productid, batchnumber, productname, supplier, quantity, reason, date, new DatabaseHelper.ReturnInsertedListener() {
            @Override
            public void onSaveSuccess() {
                fetchData();
            }

            @Override
            public void onSaveFailed(String errorMessage) {
                Log.e("ReturnPurchaseFragment", "❌ Failed to insert return product: " + errorMessage);
            }
        });

        clearFields();
        Toast.makeText(getContext(), "Return Purchase Adding...", Toast.LENGTH_SHORT).show();
    }

    private void fetchData() {
        supabaseAuthApi.getAllReturnPurchase(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<ReturnPurchase>>() {
            @Override
            public void onResponse(Call<List<ReturnPurchase>> call, Response<List<ReturnPurchase>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                } else {
                    Log.e("Supabase", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<ReturnPurchase>> call, Throwable t) {
                Log.e("Supabase", "Failed to fetch data", t);
            }
        });
    }

    private void populateTable(List<ReturnPurchase> returnpurchases) {
        tableLayout.removeAllViews();
        addTableHeader();

        for (ReturnPurchase returnpurchase : returnpurchases) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


            TextView TransactionNumberText = createStyledCell(returnpurchase.getTransactionNumber());
            TextView ProductIdText = createStyledCell(returnpurchase.getProductId());
            TextView BatchNumberText = createStyledCell(returnpurchase.getBatchNumber());
            TextView ProductNameText = createStyledCell(returnpurchase.getProductName());
            TextView SupplierText = createStyledCell(returnpurchase.getSupplier());
            TextView QuantityText = createStyledCell(returnpurchase.getQuantity());
            TextView ReturnReasonText = createStyledCell(returnpurchase.getReturnReason());
            TextView ReturnDateText = createStyledCell(returnpurchase.getReturnDate());


            row.addView(TransactionNumberText);
            row.addView(ProductIdText);
            row.addView(BatchNumberText);
            row.addView(ProductNameText);
            row.addView(SupplierText);
            row.addView(QuantityText);
            row.addView(ReturnReasonText);
            row.addView(ReturnDateText);

            tableLayout.addView(row);
        }
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue));

        String[] headers = {"Transaction Number", "Product ID", "Batch Number", "Product Name", "Supplier", "Quantity", "Return Reason", "Return Date"};
        for (String header : headers) {
            TextView textView = createStyledHeader(header);
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    private TextView createStyledCell(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setBackgroundResource(R.drawable.table_cell_border);
        textView.setTextColor(getResources().getColor(R.color.table_text));
        textView.setBackgroundColor(getResources().getColor(R.color.table_background));
        return textView;
    }

    private TextView createStyledHeader(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        return textView;
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


}