package com.example.emjiposinv;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPurchaseFragment extends Fragment {
    private static final int REQUEST_BLUETOOTH_PERMISSION = 101;

    private DatabaseHelper databaseHelper;
    private BluetoothPrinterHelper printerHelper; // Create an instance of BluetoothPrinterHelper

    private BluetoothAdapter bluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 1;

    private EditText inputProductId, inputQuantity, txtCash, txtDiscount, txtReference, txtCustomerName;

    private Spinner spinnerBatchNumber;

    private TextView inputProductPurchase, inputSupplier, inputUnitPrice, txtSubtotal, textView10, textView13, txtChange, textView9, txtTotal;
    private String transacnum, paymethod, purchaseprice;

    private Integer quantity;
    private Button btnPrintReceipt, btnPDFReceipt;
    private Button btnAdd, searchButton;
    private Button btnConnectPrinter;

    private RadioButton rbcash, rbgcash;

    private TableLayout tableLayout;

    private List<String> BatchList = new ArrayList<>();
    private SupabaseAuthApi supabaseAuthApi;

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHF" +
            "lbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2d" +
            "WF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";

    private static final String CONTENT_TYPE = "application/json";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_purchase, container, false);


    }

    private void printReceipt() {
        if (!isAdded() || printerHelper == null || printerHelper.getOutputStream() == null) {
            Toast.makeText(requireContext(), "Printer not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logor);
        printerHelper.printImage(logoBitmap);
    }


    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // ✅ Initialize Database
        databaseHelper = new DatabaseHelper(requireContext());

        printerHelper = new BluetoothPrinterHelper();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Get Bluetooth adapter

        // Request Bluetooth permission when the fragment is loaded
        requestBluetoothPermission();

        btnConnectPrinter = view.findViewById(R.id.btnConnectPrinter);
        if (btnConnectPrinter == null) {
            Log.e("NewPurchaseFragment", "btnConnectPrinter is NULL! Check XML layout.");
        } else {
            btnConnectPrinter.setOnClickListener(v -> checkAndEnableBluetooth());
        }


        rbcash = view.findViewById(R.id.rbcash);
        rbgcash = view.findViewById(R.id.rbgcash);

        textView10 = view.findViewById(R.id.textView10);
        txtSubtotal = view.findViewById(R.id.txtsubtotal);
        textView13 = view.findViewById(R.id.textView13);
        txtChange = view.findViewById(R.id.txtchange);
        txtCash = view.findViewById(R.id.txtcash);
        txtDiscount = view.findViewById(R.id.txtdiscountlabel);
        txtReference = view.findViewById(R.id.txtreference);
        textView9 = view.findViewById(R.id.textView9);
        txtTotal = view.findViewById(R.id.txttotal);
        txtCustomerName = view.findViewById(R.id.txtcustomername);

        // Find Table
        tableLayout = view.findViewById(R.id.tableLayout);

        // Add listeners
        txtCash.addTextChangedListener(new CalculationWatcher());
        txtDiscount.addTextChangedListener(new CalculationWatcher());

        // Initially hide all related views
        hideViews(textView13, txtChange, txtCash, txtDiscount, txtReference, textView9, txtTotal, textView10, txtSubtotal);

        rbcash.setChecked(true);
        // ✅ Automatically "click" rbgcash to trigger listeners
        rbcash.post(() -> rbcash.performClick());

        // Radio button click listeners
        rbgcash.setOnClickListener(v -> {
            rbcash.setChecked(false);
            showViews(txtDiscount, txtReference, textView9, txtTotal, textView10, txtSubtotal);
            hideViews(textView13, txtChange, txtCash);
            txtChange.setText("0.00");
            txtCash.setText("");
            paymethod = "GCash";
        });

        rbcash.setOnClickListener(v -> {
            rbgcash.setChecked(false);
            showViews(textView13, txtChange, txtCash, txtDiscount, textView9, txtTotal, textView10, txtSubtotal);
            hideViews(txtReference);
            txtReference.setText("");
            txtChange.setText("0.00");
            paymethod = "Cash";
        });

        searchButton = view.findViewById(R.id.btnsearch);


        searchButton.setOnClickListener(v -> {
            String productid = inputProductId.getText().toString();
            // ✅ Fetch Batch Numbers Based on Product ID
            fetchBatchtoSpinner(productid);

            // ✅ Check if the product exists in Supabase
            databaseHelper.getProductByBarcode(productid, new ProductCallback() {
                @Override
                public void onProductFound(Product product) {
                    inputProductPurchase.setText(product.getProductName());
                    inputSupplier.setText(product.getSupplier());


                    btnAdd.setEnabled(true);
                }

                @Override
                public void onProductNotFound() {
                    inputProductPurchase.setText("");
                    inputSupplier.setText("");
                    inputUnitPrice.setText("");
                    inputQuantity.setText("");

                    clearBatchSpinner();
                    btnPrintReceipt.setEnabled(false);
                    btnAdd.setEnabled(false);
                    Toast.makeText(getContext(), "Product not found!", Toast.LENGTH_SHORT).show();
                }
            });

        });

        btnPDFReceipt = view.findViewById(R.id.btnpdfreceipt);
        btnPDFReceipt.setOnClickListener(V ->{

            double change = parseDouble(txtChange.getText().toString());
            String dscnt = txtDiscount.getText().toString();
            if (paymethod.equals("Cash")) {
                String cash = txtCash.getText().toString();
                if (cash.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter the cash!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dscnt.isEmpty()) {
                    txtDiscount.setText("0");
                    //Toast.makeText(requireContext(), "Enter the discount!", Toast.LENGTH_SHORT).show();
                    //return;
                }
                if (change < 0) {
                    Toast.makeText(requireContext(), "Cash is insufficient!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                String reference = txtReference.getText().toString();
                if (dscnt.isEmpty()) {
                    txtDiscount.setText("0");
                    //Toast.makeText(requireContext(), "Enter the discount!", Toast.LENGTH_SHORT).show();
                    //return;
                }
                if (reference.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter the reference number!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (tableLayout == null) {
                Toast.makeText(requireContext(), "⚠️ TableLayout is not initialized!", Toast.LENGTH_SHORT).show();
                Log.e("NewPurchaseFragment", "⚠️ TableLayout is NULL! Check XML.");
                return;
            }

            List<ProductUpdate> productUpdates = getProductsFromTable(); // ✅ Get product list from table
            Log.d("NewPurchaseFragment", "🛒 Products to update: " + productUpdates.size());

            if (productUpdates.isEmpty()) {
                Toast.makeText(requireContext(), "⚠️ No products in table!", Toast.LENGTH_SHORT).show();
                Log.e("NewPurchaseFragment", "⚠️ Product list is empty!");
                return;
            }

            Log.d("NewPurchaseFragment", "📦 Calling fetchAndUpdateStock...");

            databaseHelper.fetchAndUpdateStock(productUpdates, new DatabaseHelper.StockUpdateListener() {
                @Override
                public void onStockUpdated() {
                    Log.d("NewPurchaseFragment", "✅ Stock updated successfully!");

                    // ✅ Collect data from UI
                    String transactionNumber = generateTransactionNumber();
                    transacnum = transactionNumber;
                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                    float subtotal = Float.parseFloat(txtSubtotal.getText().toString().trim());
                    float discount = Float.parseFloat(txtDiscount.getText().toString().trim());
                    float totalAmount = Float.parseFloat(txtTotal.getText().toString().trim());

                    float cash, change;
                    if (paymethod.equals("Cash")) {
                        cash = Float.parseFloat(txtCash.getText().toString().trim());
                        change = Float.parseFloat(txtChange.getText().toString().trim());
                    } else {
                        cash = 0;
                        change = 0;
                    }

                    String referenceNumber = txtReference.getText().toString().trim();
                    String paymentMethod = rbcash.isChecked() ? "Cash" : "GCash";
                    String customername = txtCustomerName.getText().toString();

                    // ✅ Prepare history list from table rows
                    List<Map<String, Object>> salesReportList = new ArrayList<>();
                    for (int i = 1; i < tableLayout.getChildCount(); i++) {
                        TableRow row = (TableRow) tableLayout.getChildAt(i);

                        String productId = ((TextView) row.getChildAt(0)).getText().toString();
                        String batchNumber = ((TextView) row.getChildAt(1)).getText().toString();
                        String productName = ((TextView) row.getChildAt(2)).getText().toString();
                        String supplier = ((TextView) row.getChildAt(3)).getText().toString();
                        float purchasePrice = Float.parseFloat(((TextView) row.getChildAt(4)).getText().toString());
                        float unitPrice = Float.parseFloat(((TextView) row.getChildAt(5)).getText().toString());
                        int quantity = Integer.parseInt(((TextView) row.getChildAt(6)).getText().toString());
                        float totalPrice = unitPrice * quantity;
                        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                        Map<String, Object> salesReportEntry = new HashMap<>();
                        salesReportEntry.put("transaction_number", transactionNumber);
                        salesReportEntry.put("product_id", productId);
                        salesReportEntry.put("batch_number", batchNumber);
                        salesReportEntry.put("product_name", productName);
                        salesReportEntry.put("quantity", quantity);
                        salesReportEntry.put("unit_price", unitPrice);
                        salesReportEntry.put("total_price", totalPrice);
                        salesReportEntry.put("supplier", supplier);
                        salesReportEntry.put("purchase_price", purchasePrice);
                        salesReportEntry.put("date", date);

                        salesReportList.add(salesReportEntry);
                    }

                    // ✅ Save report and history to Supabase
                    databaseHelper.saveSalesReportAndTransactionHistory(
                            transactionNumber,
                            subtotal,
                            discount,
                            totalAmount,
                            change,
                            dateTime,
                            paymentMethod,
                            cash,
                            referenceNumber,
                            customername,
                            salesReportList,
                            new DatabaseHelper.SalesSaveListener() {
                                @Override
                                public void onSaveSuccess() {
                                    Toast.makeText(requireContext(), "✅ Sales data saved successfully!", Toast.LENGTH_SHORT).show();
                                    exportReceiptToPDF(); // Only export receipt to PDF if saving to Supabase is successful
                                    updateAllProductStatuses();
                                    // Keep the first row (index 0)
                                    int childCount = tableLayout.getChildCount();
                                    if (childCount > 1) {
                                        tableLayout.removeViews(1, childCount - 1); // Remove rows starting from index 1
                                    }
                                    if (paymethod.equals("Cash")) {
                                        txtCash.setText("");
                                        txtDiscount.setText("");
                                        txtCustomerName.setText("");
                                        txtSubtotal.setText("0.00");
                                        txtTotal.setText("0.00");
                                        txtChange.setText("0.00");
                                    } else {
                                        txtReference.setText("");
                                        txtDiscount.setText("");
                                        txtCustomerName.setText("");
                                        txtSubtotal.setText("0.00");
                                        txtTotal.setText("0.00");
                                    }
                                }

                                @Override
                                public void onSaveFailed(String errorMessage) {
                                    Toast.makeText(requireContext(), "❌ Failed to save sales data: " + errorMessage, Toast.LENGTH_LONG).show();
                                    Log.e("NewPurchaseFragment", "❌ Failed to save sales: " + errorMessage);
                                }
                            }
                    );
                }

                @Override
                public void onStockUpdateFailed(String errorMessage) {
                    Log.e("NewPurchaseFragment", "❌ Stock update failed: " + errorMessage);
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnPrintReceipt = view.findViewById(R.id.btnprint);
        btnPrintReceipt.setOnClickListener(v -> {

            if (printerHelper == null || printerHelper.getOutputStream() == null) {
                Toast.makeText(requireContext(), "Printer not connected!", Toast.LENGTH_SHORT).show();
                return;
            }


            double change = parseDouble(txtChange.getText().toString());
            String dscnt = txtDiscount.getText().toString();
            if (paymethod.equals("Cash")) {
                String cash = txtCash.getText().toString();
                if (cash.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter the cash!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dscnt.isEmpty()) {
                    txtDiscount.setText("0");
                    //Toast.makeText(requireContext(), "Enter the discount!", Toast.LENGTH_SHORT).show();
                    //return;
                }
                if (change < 0) {
                    Toast.makeText(requireContext(), "Cash is insufficient!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                String reference = txtReference.getText().toString();
                if (dscnt.isEmpty()) {
                    txtDiscount.setText("0");
                    //Toast.makeText(requireContext(), "Enter the discount!", Toast.LENGTH_SHORT).show();
                    //return;
                }
                if (reference.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter the reference number!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (tableLayout == null) {
                Toast.makeText(requireContext(), "⚠️ TableLayout is not initialized!", Toast.LENGTH_SHORT).show();
                Log.e("NewPurchaseFragment", "⚠️ TableLayout is NULL! Check XML.");
                return;
            }

            List<ProductUpdate> productUpdates = getProductsFromTable(); // ✅ Get product list from table
            Log.d("NewPurchaseFragment", "🛒 Products to update: " + productUpdates.size());

            if (productUpdates.isEmpty()) {
                Toast.makeText(requireContext(), "⚠️ No products in table!", Toast.LENGTH_SHORT).show();
                Log.e("NewPurchaseFragment", "⚠️ Product list is empty!");
                return;
            }

            Log.d("NewPurchaseFragment", "📦 Calling fetchAndUpdateStock...");

            databaseHelper.fetchAndUpdateStock(productUpdates, new DatabaseHelper.StockUpdateListener() {
                @Override
                public void onStockUpdated() {
                    Log.d("NewPurchaseFragment", "✅ Stock updated successfully!");

                    // ✅ Collect data from UI
                    String transactionNumber = generateTransactionNumber();
                    transacnum = transactionNumber;
                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                    float subtotal = Float.parseFloat(txtSubtotal.getText().toString().trim());
                    float discount = Float.parseFloat(txtDiscount.getText().toString().trim());
                    float totalAmount = Float.parseFloat(txtTotal.getText().toString().trim());

                    float cash, change;
                    if (paymethod.equals("Cash")) {
                        cash = Float.parseFloat(txtCash.getText().toString().trim());
                        change = Float.parseFloat(txtChange.getText().toString().trim());
                    } else {
                        cash = 0;
                        change = 0;
                    }

                    String referenceNumber = txtReference.getText().toString().trim();
                    String paymentMethod = rbcash.isChecked() ? "Cash" : "GCash";
                    String customername = txtCustomerName.getText().toString();

                    // ✅ Prepare history list from table rows
                    List<Map<String, Object>> salesReportList = new ArrayList<>();
                    for (int i = 1; i < tableLayout.getChildCount(); i++) {
                        TableRow row = (TableRow) tableLayout.getChildAt(i);

                        String productId = ((TextView) row.getChildAt(0)).getText().toString();
                        String batchNumber = ((TextView) row.getChildAt(1)).getText().toString();
                        String productName = ((TextView) row.getChildAt(2)).getText().toString();
                        String supplier = ((TextView) row.getChildAt(3)).getText().toString();
                        float purchasePrice = Float.parseFloat(((TextView) row.getChildAt(4)).getText().toString());
                        float unitPrice = Float.parseFloat(((TextView) row.getChildAt(5)).getText().toString());
                        int quantity = Integer.parseInt(((TextView) row.getChildAt(6)).getText().toString());
                        float totalPrice = unitPrice * quantity;
                        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                        Map<String, Object> salesReportEntry = new HashMap<>();
                        salesReportEntry.put("transaction_number", transactionNumber);
                        salesReportEntry.put("product_id", productId);
                        salesReportEntry.put("batch_number", batchNumber);
                        salesReportEntry.put("product_name", productName);
                        salesReportEntry.put("quantity", quantity);
                        salesReportEntry.put("unit_price", unitPrice);
                        salesReportEntry.put("total_price", totalPrice);
                        salesReportEntry.put("supplier", supplier);
                        salesReportEntry.put("purchase_price", purchasePrice);
                        salesReportEntry.put("date", date);

                        salesReportList.add(salesReportEntry);
                    }

                    // ✅ Save report and history to Supabase
                    databaseHelper.saveSalesReportAndTransactionHistory(
                            transactionNumber,
                            subtotal,
                            discount,
                            totalAmount,
                            change,
                            dateTime,
                            paymentMethod,
                            cash,
                            referenceNumber,
                            customername,
                            salesReportList,
                            new DatabaseHelper.SalesSaveListener() {
                                @Override
                                public void onSaveSuccess() {
                                    Toast.makeText(requireContext(), "✅ Sales data saved successfully!", Toast.LENGTH_SHORT).show();
                                    printReceiptWithLogo(); // Only print if saving to Supabase is successful
                                    updateAllProductStatuses();
                                    // Keep the first row (index 0)
                                    int childCount = tableLayout.getChildCount();
                                    if (childCount > 1) {
                                        tableLayout.removeViews(1, childCount - 1); // Remove rows starting from index 1
                                    }
                                    if (paymethod.equals("Cash")) {
                                        txtCash.setText("");
                                        txtDiscount.setText("");
                                        txtCustomerName.setText("");
                                        txtSubtotal.setText("0.00");
                                        txtTotal.setText("0.00");
                                        txtChange.setText("0.00");
                                    } else {
                                        txtReference.setText("");
                                        txtDiscount.setText("");
                                        txtCustomerName.setText("");
                                        txtSubtotal.setText("0.00");
                                        txtTotal.setText("0.00");
                                    }
                                }

                                @Override
                                public void onSaveFailed(String errorMessage) {
                                    Toast.makeText(requireContext(), "❌ Failed to save sales data: " + errorMessage, Toast.LENGTH_LONG).show();
                                    Log.e("NewPurchaseFragment", "❌ Failed to save sales: " + errorMessage);
                                }
                            }
                    );
                }

                @Override
                public void onStockUpdateFailed(String errorMessage) {
                    Log.e("NewPurchaseFragment", "❌ Stock update failed: " + errorMessage);
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });






        // Initialize the total amount TextView
        // Find Inputs
        inputProductId = view.findViewById(R.id.txtproductid);
        spinnerBatchNumber = view.findViewById(R.id.ddbatchnumber3);
        inputProductPurchase = view.findViewById(R.id.txtproductpurchase);
        inputSupplier = view.findViewById(R.id.txtsupplierinpur);
        inputUnitPrice = view.findViewById(R.id.txtunitprice);
        inputQuantity = view.findViewById(R.id.txtquantity);
        btnAdd = view.findViewById(R.id.btnadd);
        Button btnScan = view.findViewById(R.id.btnscan);

        btnScan.setOnClickListener(v -> startBarcodeScanner());

        clearBatchSpinner();

        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        spinnerBatchNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBatch = spinnerBatchNumber.getSelectedItem().toString();
                String selectedProductId = inputProductId.getText().toString();

                if (!selectedBatch.equals("Select Batch") && !selectedProductId.isEmpty()) {
                    fetchProductByBatch(selectedProductId, selectedBatch);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Add table header
        addTableHeader(tableLayout);

        // Add Button Click Listener
        btnAdd.setOnClickListener(v -> {

            if (inputQuantity.getText().toString().trim().equals("0")) {
                inputQuantity.setError("Enter correct quantity");
                return;
            }

            int enteredQuantity;

            try {
                enteredQuantity = Integer.parseInt(inputQuantity.getText().toString().trim());

            } catch (NumberFormatException e) {
                inputQuantity.setError("Enter a valid quantity");
                return;
            }

            if (enteredQuantity > quantity) {
                inputQuantity.setError("Quantity exceeds available stock");
                return;
            }

            addTableRow(tableLayout, inputProductId, spinnerBatchNumber, inputProductPurchase, inputSupplier, purchaseprice, inputUnitPrice, inputQuantity);
        });

    }

    private String generateTransactionNumber() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        int random = new Random().nextInt(90000) + 10000; // 5-digit random number
        return timestamp + random;
    }

    private List<ProductUpdate> getProductsFromTable() {
        List<ProductUpdate> productUpdates = new ArrayList<>();

        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            String productId = ((TextView) row.getChildAt(0)).getText().toString();
            String batchNumber = ((TextView) row.getChildAt(1)).getText().toString();
            String quantityText = ((TextView) row.getChildAt(6)).getText().toString();

            if (productId.isEmpty() || batchNumber.isEmpty() || quantityText.isEmpty()) {
                Log.e("NewPurchaseFragment", "⚠️ Empty fields detected in row " + i);
                continue; // Skip this row instead of crashing
            }

            int quantityToDeduct;
            try {
                quantityToDeduct = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                Log.e("NewPurchaseFragment", "❌ Invalid quantity in row " + i + ": " + quantityText);
                continue; // Skip invalid rows
            }

            productUpdates.add(new ProductUpdate(productId, batchNumber, quantityToDeduct));
        }

        Log.d("NewPurchaseFragment", "✅ Extracted " + productUpdates.size() + " product updates.");
        return productUpdates;
    }
    private void printReceiptWithLogo() {
        if (printerHelper == null || printerHelper.getOutputStream() == null) {
            Toast.makeText(requireContext(), "Printer not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = AuthManager.Role;
        String supplier = AuthManager.Supplier;

        try {
            OutputStream outputStream = printerHelper.getOutputStream();

                // Print Store Logo
                Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo3);
                printerHelper.printImage(logoBitmap);

                // Print Store Info
                printerHelper.printText("Address: WE 35-36 Meycauayan ", false, false, 1);
                printerHelper.printText("Market Brgy Zamora,", false, false, 1);
                printerHelper.printText("Meycauayan, Philippines", false, false, 1);
                printerHelper.printText("NON-VATReg.TIN:494-729-289-00000", false, false, 1);
                printerHelper.printText("Contact: 09238140812/09154164198", false, false, 1);
                printerHelper.printText("Email: Ecql1975@gmail.com", false, false, 1);
                printerHelper.printText("FB Page: EMJI Frozen Foods -", false, false, 1);
                printerHelper.printText("All Purefoods Products", false, false, 1);
                printerHelper.printText("--------------------------------", false, false, 1);

                if (role.equals("Diser")) {
                    printerHelper.printText("User: " + supplier + " " + role, false, false, 0);
                } else {
                    printerHelper.printText("User: " +  role, false, false, 0);
                }
                if (paymethod.equals("Cash")) {
                    printerHelper.printText("Payment Method: Cash", false, false, 0);
                } else {
                    printerHelper.printText("Payment Method: GCash", false, false, 0);
                }
                printerHelper.printText("Date&Time: " + new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(new Date()), false, false, 0);


                printerHelper.printText("--------------------------------", false, false, 1);

                // Print Header Row
                printerHelper.printText("Item       Qty  Price   Total", true, false, 0);
                printerHelper.printText("--------------------------------", false, false, 0);

                double subtotal = 0;
                int maxItemWidth = 12; // Adjust based on your printer width

                for (int i = 1; i < this.tableLayout.getChildCount(); i++) { // Skip header row
                    TableRow row = (TableRow) this.tableLayout.getChildAt(i);

                    String itemName = ((TextView) row.getChildAt(2)).getText().toString();
                    String quantity = ((TextView) row.getChildAt(6)).getText().toString();
                    String unitPrice = ((TextView) row.getChildAt(5)).getText().toString();
                    String total = ((TextView) row.getChildAt(7)).getText().toString();

                    subtotal += Double.parseDouble(total);

                    if (itemName.length() > maxItemWidth) {
                        // Print item name alone in one row
                        printerHelper.printText(itemName, false, false, 0);

                        // Print Qty, Price, Total on the next line
                        printerHelper.printText(String.format("%-10s %-4s %-7s %s", "", quantity, unitPrice, total), false, false, 0);
                    } else {
                        // Print normally if item name is short
                        printerHelper.printText(String.format("%-10s %-4s %-7s %s", itemName, quantity, unitPrice, total), false, false, 0);
                    }
                }

                // Get Discount from TextView (Default 0 if empty)
                String discountInput = txtDiscount.getText().toString();
                double discount = discountInput.isEmpty() ? 0.0 : Double.parseDouble(discountInput);

                double totalAmount = subtotal - discount;

                // Print Subtotal, Discount, and Total
                printerHelper.printText("--------------------------------", false, false, 0);
                printerHelper.printText(String.format("%-20s %10.2f", "Subtotal:", subtotal), false, false, 0);
                printerHelper.printText(String.format("%-20s %10.2f", "Discount:", discount), false, false, 0);
                printerHelper.printText("--------------------------------", false, false, 0);
                printerHelper.printText(String.format("%-20s %10.2f", "Total:", totalAmount), true, false, 0);


                if (paymethod.equals("Cash")) {
                    // Get Cash and Change from TextViews (Default 0 if empty)
                    String cashInput = txtCash.getText().toString();
                    String changeInput = txtChange.getText().toString();

                    double cash = cashInput.isEmpty() ? 0.0 : Double.parseDouble(cashInput);
                    double change = changeInput.isEmpty() ? 0.0 : Double.parseDouble(changeInput);

                    // Print Cash and Change aligned to the right
                    printerHelper.printText(String.format("%-20s %10.2f", "Cash:", cash), false, false, 0);
                    printerHelper.printText(String.format("%-20s %10.2f", "Change:", change), false, false, 0);
                } else {
                    // Get Cash and Change from TextViews (Default 0 if empty)
                    String referencenumInput = txtReference.getText().toString();

                    // Print Cash and Change aligned to the right
                    printerHelper.printText( "Reference Number:", false, false, 0);
                    printerHelper.printText(String.format(referencenumInput), false, false, 0);
                }

            // Print Footer
            printerHelper.printText("--------------------------------", false, false, 0);

            String customername = txtCustomerName.getText().toString();
            if (customername.length() > maxItemWidth) {
                printerHelper.printText("Customer Name: ", false, false, 0);

                printerHelper.printText(customername, false, false, 0);
            } else {
                printerHelper.printText("Customer Name: " + customername, false, false, 0);
            }

            printerHelper.printText("Signature:______________________", false, false, 0);
            printerHelper.printText(" ", false, false, 1);

                Bitmap barcodeBitmap = generateBarcode(transacnum);

                if (barcodeBitmap != null) {
                printerHelper.printText("TRANSACTION NUMBER #: ", true, false, 1);
                printerHelper.printImage(barcodeBitmap);
                printerHelper.printText(transacnum, false, false, 1);
            }
                printerHelper.printText(" ", false, false, 1);
                printerHelper.printText("RETURN PURCHASE POLICY:", true, false, 1);
                printerHelper.printText("Items bought at the stall can ", false, false, 1);
                printerHelper.printText("be returned within 2 days with", false, false, 1);
                printerHelper.printText("a receipt, but delivery items ", false, false, 1);
                printerHelper.printText("can't be returned as they are ", false, false, 1);
                printerHelper.printText("inspected by both the delivery ", false, false, 1);
                printerHelper.printText("person and customer before ", false, false, 1);
                printerHelper.printText("completing the transaction.", false, false, 1);
                printerHelper.printText("--------------------------------", false, false, 1);
                printerHelper.printText(" ", false, false, 1);

                outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error printing receipt!", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportReceiptToPDF() {
        // Define the page size (A4 or custom)
        Rectangle pageSize = new Rectangle(612, 792); // 8.5x11 inches (letter size)
        Document document = new Document(pageSize, 40, 40, 50, 50); // Set margins
        try {
            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            String tran = transacnum;
            String fileName = "Receipt_" + tran + ".pdf";
            File file = new File(pdfPath, fileName);

            // Initialize PdfWriter
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Add Logo
            Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo3); // Your logo resource
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image logo = Image.getInstance(stream.toByteArray());
            logo.scaleToFit(100, 100);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);

            // Add Store Info
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            document.add(new Paragraph("Address: WE 35-36 Meycauayan Market Brgy Zamora, Meycauayan, Philippines", normalFont));
            document.add(new Paragraph("NON-VAT Reg. TIN: 494-729-289-00000", normalFont));
            document.add(new Paragraph("Contact: 09238140812 / 09154164198 - Email: Ecql1975@gmail.com", normalFont));
            document.add(new Paragraph("FB Page: EMJI Frozen Foods - All Purefoods Products", normalFont));
            document.add(new Paragraph("_____________________________________________________________________________________________", normalFont));

            // Add User and Payment Method
            String role = AuthManager.Role;
            String supplier = AuthManager.Supplier;
            String userText = role.equals("Diser") ? supplier + " " + role : role;
            String payMethod = paymethod.equals("Cash") ? "Cash" : "GCash";
            document.add(new Paragraph("User: " + userText, normalFont));
            document.add(new Paragraph("Payment Method: " + payMethod, normalFont));
            document.add(new Paragraph("Date&Time: " + new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(new Date()), normalFont));
            document.add(new Paragraph("_____________________________________________________________________________________________", normalFont));

            // Define smaller fonts for headers and cells to reduce space
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL);

// Create the table
            PdfPTable itemTable = new PdfPTable(4);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{4, 1, 1, 1}); // More space for item name

// Header Row with reduced padding and borderless cells
            String[] headers = {"Item", "Qty", "Price", "Total"};
            for (String title : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(title, headerFont));
                headerCell.setBorder(Rectangle.NO_BORDER); // Remove borders for a cleaner look
                headerCell.setPaddingTop(2f);
                headerCell.setPaddingBottom(2f);
                itemTable.addCell(headerCell);
            }

            double subtotal = 0;

// Loop through each item in the receipt (table rows)
            for (int i = 1; i < this.tableLayout.getChildCount(); i++) {
                TableRow row = (TableRow) this.tableLayout.getChildAt(i);

                String itemName = ((TextView) row.getChildAt(2)).getText().toString();
                String quantity = ((TextView) row.getChildAt(6)).getText().toString();
                String unitPrice = ((TextView) row.getChildAt(5)).getText().toString();
                String total = ((TextView) row.getChildAt(7)).getText().toString();

                subtotal += Double.parseDouble(total);

                // Add item data cells with compact style
                String[] values = {itemName, quantity, unitPrice, total};
                for (String val : values) {
                    PdfPCell cell = new PdfPCell(new Phrase(val, cellFont));
                    cell.setBorder(Rectangle.NO_BORDER); // No border
                    cell.setPaddingTop(2f);              // Tighter vertical spacing
                    cell.setPaddingBottom(2f);
                    itemTable.addCell(cell);
                }
            }



            // ✅ Add the populated table *after* the loop
            document.add(itemTable);


            // Add discount and total calculations
            double discount = 0.0;
            String discountInput = txtDiscount.getText().toString();
            if (!discountInput.isEmpty()) {
                discount = Double.parseDouble(discountInput);
            }

            double totalAmount = subtotal - discount;

            document.add(new Paragraph("_____________________________________________________________________________________________", normalFont));
            document.add(new Paragraph(String.format("Subtotal: %.2f", subtotal), normalFont));
            document.add(new Paragraph(String.format("Discount: %.2f", discount), normalFont));
            document.add(new Paragraph("_____________________________________________________________________________________________", normalFont));
            document.add(new Paragraph(String.format("Total: %.2f", totalAmount), normalFont));

            // Add Cash and Change if paid in cash
            if (paymethod.equals("Cash")) {
                String cashInput = txtCash.getText().toString();
                String changeInput = txtChange.getText().toString();
                double cash = cashInput.isEmpty() ? 0.0 : Double.parseDouble(cashInput);
                double change = changeInput.isEmpty() ? 0.0 : Double.parseDouble(changeInput);

                document.add(new Paragraph(String.format("Cash: %.2f", cash), normalFont));
                document.add(new Paragraph(String.format("Change: %.2f", change), normalFont));
            } else {
                String referencenumInput = txtReference.getText().toString();
                document.add(new Paragraph("Reference Number: " + referencenumInput, normalFont));
            }

            // Footer (Customer Name, Signature, Barcode)
            document.add(new Paragraph("_____________________________________________________________________________________________", normalFont));
            String customername = txtCustomerName.getText().toString();
            document.add(new Paragraph("Customer Name: " + customername, normalFont));
            document.add(new Paragraph("Signature: ______________________", normalFont));
            document.add(new Paragraph(" ", normalFont));

            // Barcode (use your existing barcode generator method)
            Bitmap barcodeBitmap = generateBarcode(transacnum);
            if (barcodeBitmap != null) {
                ByteArrayOutputStream barcodeStream = new ByteArrayOutputStream();
                barcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, barcodeStream);
                Image barcodeImage = Image.getInstance(barcodeStream.toByteArray());

                barcodeImage.scaleToFit(200, 100); // Resize barcode image
                barcodeImage.setAlignment(Image.ALIGN_CENTER); // ✅ Center the barcode image
                document.add(barcodeImage);

                Paragraph transactionText = new Paragraph("TRANSACTION NUMBER #: " + transacnum, normalFont);
                transactionText.setAlignment(Element.ALIGN_CENTER); // ✅ Center the text
                document.add(transactionText);
            }


            // Return policy
            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph("RETURN PURCHASE POLICY:", normalFont));
            document.add(new Paragraph("Items bought at the stall can be returned within 2 days with a receipt.", normalFont));
            document.add(new Paragraph("Delivery items can't be returned as they are inspected by both the delivery person and customer before completing the transaction.", normalFont));
            document.add(new Paragraph("_____________________________________________________________________________________________", normalFont));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error generating PDF!", Toast.LENGTH_SHORT).show();
        }
    }




    private Bitmap generateBarcode(String data) {
        try {
            int barcodeWidth = 384;  // Max printable width for 58mm
            int barcodeHeight = 100; // You can go higher if needed (e.g., 120–160 for taller bars)

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(
                    data,
                    BarcodeFormat.CODE_128, // Good for alphanumeric transaction IDs
                    barcodeWidth,
                    barcodeHeight
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    // Helper method to calculate values
    private void calculateValues() {
        double subtotal = parseDouble(txtSubtotal.getText().toString());
        double discount = parseDouble(txtDiscount.getText().toString());
        double total = subtotal - discount;
        txtTotal.setText(String.format("%.2f", total));

        double cash = parseDouble(txtCash.getText().toString());
        double change = cash - total;
        txtChange.setText(String.format("%.2f", change));
    }

    // Helper method to parse numbers safely
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // TextWatcher to auto-update values
    private class CalculationWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            calculateValues();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    // Method to add table header
    private void addTableHeader(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue));

        String[] headers = {"Product ID", "Batch Number", "Product Purchase", "Supplier", "Purchase Price", "Unit Price", "Quantity", "Amount", "Action"};
        for (String header : headers) {
            TextView textView = createStyledHeader(header);
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    inputProductId.setText(scannedCode);

                    // ✅ Fetch Batch Numbers Based on Product ID
                    fetchBatchtoSpinner(scannedCode);

                    // ✅ Check if the product exists in Supabase
                    databaseHelper.getProductByBarcode(scannedCode, new ProductCallback() {
                        @Override
                        public void onProductFound(Product product) {
                            inputProductPurchase.setText(product.getProductName());
                            inputSupplier.setText(product.getSupplier());


                            btnAdd.setEnabled(true);
                        }

                        @Override
                        public void onProductNotFound() {
                            inputProductPurchase.setText("");
                            inputSupplier.setText("");
                            inputUnitPrice.setText("");
                            inputQuantity.setText("");

                            clearBatchSpinner();
                            btnPrintReceipt.setEnabled(false);
                            btnAdd.setEnabled(false);
                            Toast.makeText(getContext(), "Product not found!", Toast.LENGTH_SHORT).show();
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
        options.setPrompt("Scan a barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(MyCaptureActivity.class);

        barcodeLauncher.launch(options);
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



    // Populate Supplier Spinner
    private void populateSpinnerBatch() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, BatchList);
        spinnerBatchNumber.setAdapter(adapter);
    }
    private void fetchProductByBatch(String productId, String batchNumber) {
        supabaseAuthApi.getProductByBatch("eq." + productId, "eq." + batchNumber, API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (!isAdded()) return; // ✅ Prevent UI updates if the fragment is not attached

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Product product = response.body().get(0);
                    purchaseprice = String.valueOf((product.getPurchasePrice()));
                    inputUnitPrice.setText(String.valueOf(product.getUnitPrice()));
                    quantity = Integer.valueOf((product.getQuantity()));
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

    private void clearBatchSpinner() {
        BatchList.clear();
        BatchList.add("Select Batch");
        populateSpinnerBatch();
    }
    // Method to add new rows dynamically
    private void addTableRow(TableLayout tableLayout, EditText inputProductId, Spinner spinnerBatchNumber, TextView inputProductPurchase, TextView inputSupplier, String purchaseprice, TextView inputUnitPrice, EditText inputQuantity) {
        String productId = inputProductId.getText().toString();
        String batchNumber = spinnerBatchNumber.getSelectedItem().toString();
        String productPurchase = inputProductPurchase.getText().toString();
        String supplier = inputSupplier.getText().toString();
        String unitPriceStr = inputUnitPrice.getText().toString();
        String quantityStr = inputQuantity.getText().toString();

        if (productId.isEmpty() || batchNumber.isEmpty() || productPurchase.isEmpty() || supplier.isEmpty() || purchaseprice.isEmpty() || unitPriceStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double unitPrice = Double.parseDouble(unitPriceStr);
        int quantity = Integer.parseInt(quantityStr);
        double amount = unitPrice * quantity;

        // Check if product with same ID, batch, and purchase already exists
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow existingRow = (TableRow) child;

                String existingProductId = ((TextView) existingRow.getChildAt(0)).getText().toString();
                String existingBatch = ((TextView) existingRow.getChildAt(1)).getText().toString();
                String existingPurchase = ((TextView) existingRow.getChildAt(2)).getText().toString();

                if (existingProductId.equals(productId) && existingBatch.equals(batchNumber) && existingPurchase.equals(productPurchase)) {
                    // Match found - update quantity and amount
                    TextView existingQuantityText = (TextView) existingRow.getChildAt(6);
                    TextView existingAmountText = (TextView) existingRow.getChildAt(7);

                    int existingQuantity = Integer.parseInt(existingQuantityText.getText().toString());
                    int newQuantity = existingQuantity + quantity;
                    double newAmount = newQuantity * unitPrice;

                    existingQuantityText.setText(String.valueOf(newQuantity));
                    existingAmountText.setText(String.format(Locale.getDefault(), "%.2f", newAmount));

                    calculateTotalAmount(tableLayout); // Recalculate total

                    clearBatchSpinner();
                    inputProductId.setText("");
                    inputProductPurchase.setText("");
                    inputSupplier.setText("");
                    inputQuantity.setText("");
                    inputUnitPrice.setText("");
                    btnAdd.setEnabled(false);
                    btnPrintReceipt.setEnabled(true);
                    return; // Exit after updating
                }
            }
        }

        // If no match found, add a new row
        TableRow row = new TableRow(requireContext());

        TextView idText = createStyledCell(productId);
        TextView batchText = createStyledCell(batchNumber);
        TextView purchaseText = createStyledCell(productPurchase);
        TextView supplierText = createStyledCell(supplier);
        TextView purchasepriceText = createStyledCell(purchaseprice);
        TextView unitPriceText = createStyledCell(unitPriceStr);
        TextView quantityText = createStyledCell(quantityStr);
        TextView amountText = createStyledCell(String.valueOf(amount));

        int buttonSizeDp = 32;
        float scale = getResources().getDisplayMetrics().density;
        int buttonSizePx = (int) (buttonSizeDp * scale + 0.5f);

        TableRow.LayoutParams buttonLayoutParams = new TableRow.LayoutParams(0, buttonSizePx, 1f);
        buttonLayoutParams.setMargins(4, 4, 4, 4);

        ImageButton btnIncrease = new ImageButton(requireContext());
        btnIncrease.setImageResource(R.drawable.plus);
        btnIncrease.setBackgroundResource(android.R.color.transparent);
        btnIncrease.setLayoutParams(buttonLayoutParams);
        btnIncrease.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        btnIncrease.setOnClickListener(v -> {
            showPinConfirmationDialog(() -> {
                updateQuantity(quantityText, amountText, unitPrice, 1, tableLayout);
            });
        });

        ImageButton btnDecrease = new ImageButton(requireContext());
        btnDecrease.setImageResource(R.drawable.minus);
        btnDecrease.setBackgroundResource(android.R.color.transparent);
        btnDecrease.setLayoutParams(buttonLayoutParams);
        btnDecrease.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        btnDecrease.setOnClickListener(v -> {
            showPinConfirmationDialog(() -> {
                int currentQty = Integer.parseInt(quantityText.getText().toString());

                if (currentQty <= 1) {
                    // Quantity will become 0 → ask for confirmation before deleting
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Removal")
                            .setMessage("Quantity will become zero. Do you want to remove this product from the list?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                tableLayout.removeView(row);
                                calculateTotalAmount(tableLayout);
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    // Just decrease quantity
                    updateQuantity(quantityText, amountText, unitPrice, -1, tableLayout);
                    calculateTotalAmount(tableLayout);
                }
            });
        });


        ImageButton btnDelete = new ImageButton(requireContext());
        btnDelete.setImageResource(R.drawable.x);
        btnDelete.setBackgroundResource(android.R.color.transparent);
        btnDelete.setLayoutParams(buttonLayoutParams);
        btnDelete.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        btnDelete.setOnClickListener(v -> {
            showPinConfirmationDialog(() -> {
                tableLayout.removeView(row);
                txtSubtotal.setText("");
                txtTotal.setText("");
                txtCash.setText("");
                txtDiscount.setText("");
                txtChange.setText("");
                calculateTotalAmount(tableLayout);
            });
        });

        row.addView(idText);
        row.addView(batchText);
        row.addView(purchaseText);
        row.addView(supplierText);
        row.addView(purchasepriceText);
        row.addView(unitPriceText);
        row.addView(quantityText);
        row.addView(amountText);
        row.addView(btnIncrease);
        row.addView(btnDecrease);
        row.addView(btnDelete);

        tableLayout.addView(row);

        calculateTotalAmount(tableLayout);

        clearBatchSpinner();
        inputProductId.setText("");
        inputProductPurchase.setText("");
        inputSupplier.setText("");
        inputQuantity.setText("");
        inputUnitPrice.setText("");
        btnAdd.setEnabled(false);
        btnPrintReceipt.setEnabled(true);
    }

    private void showPinConfirmationDialog(Runnable onPinCorrect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_pin_input, null);

        EditText etPinInput = dialogView.findViewById(R.id.etPinInput);

        builder.setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    String enteredPin = etPinInput.getText().toString();

                    SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String savedPin = prefs.getString("secure_pin", "1234");

                    if (enteredPin.equals(savedPin)) {
                        onPinCorrect.run();
                    } else {
                        Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    // Method to update quantity and total dynamically
    private void updateQuantity(TextView quantityText, TextView amountText, double unitPrice, int change, TableLayout tableLayout) {
        int currentQuantity = Integer.parseInt(quantityText.getText().toString());
        int newQuantity = currentQuantity + change;

        if (newQuantity < 0) return; // Prevent negative quantity

        double newAmount = newQuantity * unitPrice;

        quantityText.setText(String.valueOf(newQuantity));
        amountText.setText(String.valueOf(newAmount));

        // ✅ Recalculate total amount whenever quantity changes
        calculateTotalAmount(tableLayout);
    }

    // Method to calculate total amount of all products
    private void calculateTotalAmount(TableLayout tableLayout) {
        double totalAmount = 0;

        // Loop through all rows in the table (skip header row)
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            View rowView = tableLayout.getChildAt(i);
            if (rowView instanceof TableRow) {
                TableRow row = (TableRow) rowView;

                // Get the amount column (7th column: index 7)
                TextView amountText = (TextView) row.getChildAt(7);

                if (amountText != null) {
                    try {
                        totalAmount += Double.parseDouble(amountText.getText().toString());
                    } catch (NumberFormatException e) {
                        Log.e("TotalCalculation", "Error parsing amount", e);
                    }
                }
            }
        }

        //  Set total amount in the TextView
        txtSubtotal.setText(String.format("%.2f", totalAmount));
    }

    // Styled Header Method
    private TextView createStyledHeader(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        return textView;
    }

    // Styled Cell Method
    private TextView createStyledCell(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextColor(getResources().getColor(R.color.table_text)); // Auto-adjusts to light/dark mode
        textView.setBackgroundColor(getResources().getColor(R.color.table_background)); // Ensures contrast
        return textView;
    }




    private void checkAndEnableBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is disabled, prompt user to enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth is already enabled, proceed to show devices
            showBluetoothDevices();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // User enabled Bluetooth, now show available devices
                showBluetoothDevices();
            } else {
                // User refused to enable Bluetooth
                Toast.makeText(requireContext(), "Bluetooth is required to connect to a printer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Bluetooth Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showBluetoothDevices() {
        // Check permission before proceeding
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                return;
            }
        }

        List<BluetoothDevice> devices = printerHelper.getAvailableDevices(requireContext());

        if (devices == null || devices.isEmpty()) {
            Toast.makeText(requireContext(), "No Bluetooth devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select a Bluetooth Printer");

        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        for (BluetoothDevice device : devices) {
            // Ensure device name is not null to prevent crashes
            String deviceName = (device.getName() != null) ? device.getName() : "Unknown Device";
            deviceAdapter.add(deviceName + "\n" + device.getAddress());
        }

        ListView listView = new ListView(requireContext());
        listView.setAdapter(deviceAdapter);
        builder.setView(listView);

        AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice selectedDevice = devices.get(position);
            connectToSelectedPrinter(selectedDevice);
            dialog.dismiss();
        });
    }



    private void connectToSelectedPrinter(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                return;
            }
        }

        if (printerHelper.connectToPrinter(requireContext(), device)) {
            Toast.makeText(requireContext(), "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to connect to printer", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAllProductStatuses() {


        supabaseAuthApi.getAllProducts(API_KEY, AUTH_TOKEN, CONTENT_TYPE)
                .enqueue(new Callback<List<ProductStatus>>() {
                    @Override
                    public void onResponse(Call<List<ProductStatus>> call, Response<List<ProductStatus>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ProductStatus> productstatuss = response.body();

                            for (ProductStatus productstatus : productstatuss) {
                                String newStatus;

                                if (productstatus .getQuantity() < 0) {
                                    newStatus = "Out of Stock";
                                } else if (productstatus .getQuantity() < 10) {
                                    newStatus = "Re Stock";
                                } else {
                                    newStatus = "In Stock";
                                }

                                // Skip update if status is already correct
                                if (newStatus.equalsIgnoreCase(productstatus.getStatus())) continue;

                                Map<String, Object> updateMap = new HashMap<>();
                                updateMap.put("status", newStatus);

                                supabaseAuthApi.updateProductStatus(
                                        API_KEY,
                                        AUTH_TOKEN,
                                        CONTENT_TYPE,
                                        "eq." + productstatus.getProductId(),       // ✅ correct format
                                        "eq." + productstatus.getBatchNumber(),     // ✅ correct format
                                        updateMap
                                ).enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            Log.d("StatusUpdate", "Updated: " + productstatus.getProductName());
                                        } else {
                                            Log.e("StatusUpdate", "Failed to update: " + productstatus.getProductName());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Log.e("StatusUpdate", "Error updating: " + productstatus.getProductName() + " - " + t.getMessage());
                                    }
                                });
                            }
                        } else {
                            Log.e("StatusUpdate", "Failed to fetch products.");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProductStatus>> call, Throwable t) {
                        Log.e("StatusUpdate", "Fetch error: " + t.getMessage());
                    }
                });
    }



}
