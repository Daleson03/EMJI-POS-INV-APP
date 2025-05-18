package com.example.emjiposinv;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManageStockFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private EditText etDate, quantity, productid;

    private String expdate;
    private Button scanButton, insertButton, searchButton;
    private TextView ProductName, SupplierName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_stock, container, false);

        databaseHelper = new DatabaseHelper(requireContext());
        scanButton = view.findViewById(R.id.btnscan3);
        insertButton = view.findViewById(R.id.btnadd2);
        searchButton = view.findViewById(R.id.btnsearch3);

        ProductName = view.findViewById(R.id.txtprodname);
        SupplierName = view.findViewById(R.id.txtsupname);

        quantity = view.findViewById(R.id.txtquaninstock);
        productid = view.findViewById(R.id.txtprodidinstock);

        scanButton.setOnClickListener(v -> startBarcodeScanner());
        insertButton.setOnClickListener(v -> addstock());

        searchButton.setOnClickListener(v -> {
            String prod_id = productid.getText().toString();
            productid.setText(prod_id);

            // Check if the product exists in Supabase
            databaseHelper.getProductByBarcode(prod_id, new ProductCallback() {
                @Override
                public void onProductFound(Product product) {
                    ProductName.setText(product.getProductName());
                    SupplierName.setText(product.getSupplier());
                    expdate = product.getExpirationDate();
                }

                @Override
                public void onProductNotFound() {
                    ProductName.setText("");
                    SupplierName.setText("");
                    quantity.setText("");
                    etDate.setText("");

                }
            });
        });


        // Initialize DatePicker EditText
        etDate = view.findViewById(R.id.etDate);
        etDate.setOnClickListener(v -> showDatePicker());

        return view;
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    productid.setText(scannedCode);

                    // Check if the product exists in Supabase
                    databaseHelper.getProductByBarcode(scannedCode, new ProductCallback() {
                        @Override
                        public void onProductFound(Product product) {
                            ProductName.setText(product.getProductName());
                            SupplierName.setText(product.getSupplier());
                            expdate = product.getExpirationDate();
                        }

                        @Override
                        public void onProductNotFound() {
                            ProductName.setText("");
                            SupplierName.setText("");
                            quantity.setText("");
                            etDate.setText("");

                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );


    private void startBarcodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt("Scan a barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(MyCaptureActivity.class);

        barcodeLauncher.launch(options);
    }

    private void addstock() {
        String productName = ProductName.getText().toString();
        String supplierName = SupplierName.getText().toString();
        String expirationDate = etDate.getText().toString();
        String productId = productid.getText().toString();
        int Quantity;

        try {
            Quantity = Integer.parseInt(quantity.getText().toString().trim());
            if (Quantity <= 0) {
                Toast.makeText(getContext(), "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        String batchnum = expirationDate.replace("/", "");

        if (productName.isEmpty() || productId.isEmpty() || supplierName.isEmpty() || expirationDate.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (expdate == null || expdate.isEmpty()) {
            //  If expdate is empty, update the product with expirationDate, batchNumber, and quantity
            databaseHelper.updateProductStock(expirationDate, productId, batchnum, Quantity);
            Toast.makeText(getContext(), "Stock Adding...", Toast.LENGTH_SHORT).show();
        } else {
            // If expdate exists, update or add new batch
            databaseHelper.updateProductStockwithBatch(expirationDate, productId, batchnum, Quantity);
            Toast.makeText(getContext(), "Stock Adding...", Toast.LENGTH_SHORT).show();
        }

        clearFields();
    }


    private void clearFields() {
        productid.setText("");
        ProductName.setText("");
        SupplierName.setText("");
        quantity.setText("");
        etDate.setText("");
    }
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatting the selected date
                    String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay ;
                    etDate.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }
}
