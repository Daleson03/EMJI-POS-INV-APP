package com.example.emjiposinv;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StockHistoryFragment extends Fragment {

    private EditText txtProductId , txtetdate;
    private Button btnScan, btnAddProd, btnPDFStockHistory;
    private Spinner supplierSpinner;
    private Spinner statusSpinner;
    private TableLayout tableLayout;

    private boolean isProductDataLoaded = false;
    private boolean isSupplierDataLoaded = false;

    private List<String> supplierList = new ArrayList<>();
    private SupabaseAuthApi supabaseAuthApi;

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6" +
            "ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSW" +
            "cq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS" +
            "IsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwI" +
            "joyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock_history, container, false);

        // Initialize UI components
        supplierSpinner = view.findViewById(R.id.ddsupplierinstock);
        statusSpinner = view.findViewById(R.id.ddstatusinstock);
        tableLayout = view.findViewById(R.id.tableLayout);

        btnAddProd = view.findViewById(R.id.btnaddprodstock);

        btnAddProd.setOnClickListener(v -> navigateToManageStockFragment());
        // Set up spinners
        setupSpinner(statusSpinner, getStatusList());

        btnPDFStockHistory = view.findViewById(R.id.btnpropdf3);

        btnPDFStockHistory.setOnClickListener(v -> exportStockHistoryToPDF());

        // Initialize Supabase API
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        txtProductId = view.findViewById(R.id.txtproductidinstock);
        txtetdate = view.findViewById(R.id.etDate3);
        btnScan = view.findViewById(R.id.btnscan8);

        btnScan.setOnClickListener(v -> launchBarcodeScanner());


        txtetdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Fetch and display product data
        fetchData();

        //  Fetch suppliers from Supabase
        fetchSupplierstoSpinnerinprodlist();

        txtProductId.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTable(tableLayout, s.toString().trim(), txtetdate.getText().toString().trim());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        txtetdate.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTable(tableLayout, txtProductId.getText().toString().trim(), s.toString().trim());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });


        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isProductDataLoaded && isSupplierDataLoaded) { //  Only filter when data is loaded
                    filterTable();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        supplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isProductDataLoaded && isSupplierDataLoaded) { //  Only filter when data is loaded
                    filterTable();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        return view;
    }
    private void navigateToManageStockFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, new ManageStockFragment()); // Ensure this ID exists in your layout
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    txtProductId.setText(result.getContents()); // Set scanned barcode to EditText
                }
            }
    );

    private void launchBarcodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setCaptureActivity(MyCaptureActivity.class); // Optional custom activity

        barcodeLauncher.launch(options);
    }

    private void filterTable(TableLayout tableLayout, String productQuery, String datereceiveQuery) {
        int rowCount = tableLayout.getChildCount();
        Log.d("Filter", "Total Rows: " + rowCount); //  Debugging log

        for (int i = 1; i < rowCount; i++) { // Start from 1 to skip header row
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                TextView ProductIDView = (TextView) row.getChildAt(0); //  First column

                //  Ensure row has at least 11 columns before accessing index 10
                if (row.getChildCount() > 10) {
                    TextView DateReceiveView = (TextView) row.getChildAt(10);

                    if (ProductIDView != null && DateReceiveView != null) {
                        String ProductID = ProductIDView.getText().toString().trim().toLowerCase();
                        String DateReceive = DateReceiveView.getText().toString().trim().toLowerCase();

                        //  Convert input date to match "YYYY-MM-DD" format
                        String formattedDateQuery = normalizeDateFormat(datereceiveQuery.trim());

                        boolean matchesProduct = productQuery.isEmpty() || ProductID.contains(productQuery.toLowerCase());
                        boolean matchesDateReceive = datereceiveQuery.isEmpty() || DateReceive.contains(formattedDateQuery);

                        row.setVisibility(View.VISIBLE); // Reset visibility before filtering

                        //  Row is visible if BOTH conditions match or if one field is empty
                        if (matchesProduct && matchesDateReceive) {
                            row.setVisibility(View.VISIBLE);
                        } else {
                            row.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Log.e("Filter", "Row " + i + " does not have enough columns.");
                }
            }
        }
    }
    private String normalizeDateFormat(String inputDate) {
        if (inputDate.isEmpty()) return "";

        String[] parts = inputDate.split("-");
        if (parts.length == 3) {
            // Ensure two-digit month and day
            String year = parts[0];
            String month = (parts[1].length() == 1) ? "0" + parts[1] : parts[1];
            String day = (parts[2].length() == 1) ? "0" + parts[2] : parts[2];

            return year + "-" + month + "-" + day;
        }
        return inputDate; // Return original if format is unknown
    }


    private void fetchSupplierstoSpinnerinprodlist() {
        supabaseAuthApi.getSupplierstoSpinnerinprodlist(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    supplierList.clear();
                    supplierList.add("All"); //  Default option

                    for (Supplier supplier : response.body()) {
                        supplierList.add(supplier.getSupplierName());
                    }
                    populateSpinner();
                    isSupplierDataLoaded = true;
                    checkAndFilterTable(); //  Ensure filtering only happens when both data sources are ready
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch suppliers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Populate Supplier Spinner
    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, supplierList);
        supplierSpinner.setAdapter(adapter);
    }

    // Setup Dropdowns
    private void setupSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
    }




    // Method to get status list
    private List<String> getStatusList() {
        List<String> items = new ArrayList<>();
        items.add("All");
        items.add("In Stock");
        items.add("Re Stock");
        items.add("Out of Stock");
        return items;
    }

    private void fetchData() {
        supabaseAuthApi.getAllStockHistory(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Stock>>() {
            @Override
            public void onResponse(Call<List<Stock>> call, Response<List<Stock>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                    isProductDataLoaded = true;
                    checkAndFilterTable();
                } else {
                    Log.e("Supabase", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Stock>> call, Throwable t) {
                Log.e("Supabase", "Failed to fetch data", t);
            }
        });
    }

    private void checkAndFilterTable() {
        if (isProductDataLoaded && isSupplierDataLoaded) {
            filterTable();
        }
    }
    private void populateTable(List<Stock> stocks) {
        tableLayout.removeAllViews(); // Clear existing rows
        addTableHeader(); // Add the header row

        for (Stock stock : stocks) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView idText = createStyledCell(String.valueOf(stock.getProductId()));
            TextView batchText = createStyledCell(stock.getBatchNumber());
            TextView expirationdateText = createStyledCell(stock.getExpirationDate());
            TextView nameText = createStyledCell(stock.getProductName());
            TextView supplierText = createStyledCell(stock.getSupplier());
            TextView unitText = createStyledCell(stock.getUnit());
            TextView unitPriceText = createStyledCell(String.valueOf(stock.getUnitPrice()));
            TextView purchasePriceText = createStyledCell(String.valueOf(stock.getPurchasePrice()));
            TextView qtyText = createStyledCell(stock.getQuantity());
            TextView statusText = createStyledCell(stock.getStatus());
            TextView receivedateText = createStyledCell(stock.getReceiveDate());
            TextView receivetimeText = createStyledCell(stock.getReceiveTime());

            // Create an ImageButton for the action column
            row.addView(idText);
            row.addView(batchText);
            row.addView(expirationdateText);
            row.addView(nameText);
            row.addView(supplierText);
            row.addView(unitText);
            row.addView(unitPriceText);
            row.addView(purchasePriceText);
            row.addView(qtyText);
            row.addView(statusText);// Add button to row
            row.addView(receivedateText);
            row.addView(receivetimeText);

            tableLayout.addView(row);
        }
    }


    private void addTableHeader() {
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue));// Blue background

        String[] headers = {"Product ID", "Batch Number", "Expiration Date", "Product Name", "Supplier", "Unit", "Unit Price","Purchase Price", "Quantity", "Status", "Receive Date", "Receive Time"};
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
        textView.setTextColor(getResources().getColor(R.color.table_text)); // Auto-adjusts to light/dark mode
        textView.setBackgroundColor(getResources().getColor(R.color.table_background)); // Ensures contrast // Add border
        return textView;
    }
    private TextView createStyledHeader(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(getResources().getColor(android.R.color.white)); // White text
        return textView;
    }

    private void filterTable() {
        //  Check if spinners have values before calling .toString()
        String selectedStatus = (statusSpinner.getSelectedItem() != null) ? statusSpinner.getSelectedItem().toString() : "";
        String selectedSupplier = (supplierSpinner.getSelectedItem() != null) ? supplierSpinner.getSelectedItem().toString() : "";

        Log.d("Filter", "Filtering with Supplier: " + selectedSupplier + ", Status: " + selectedStatus);

        // Loop through all rows in the TableLayout
        for (int i = 1; i < tableLayout.getChildCount(); i++) { // Assuming first row is header
            View rowView = tableLayout.getChildAt(i);

            if (rowView instanceof TableRow) {
                TableRow row = (TableRow) rowView;

                // Check if row has enough children before accessing
                if (row.getChildCount() > 7) {
                    TextView supplierText = (TextView) row.getChildAt(4); // Supplier column
                    TextView statusText = (TextView) row.getChildAt(9);   // Status column

                    String supplierValue = (supplierText != null) ? supplierText.getText().toString() : "";
                    String statusValue = (statusText != null) ? statusText.getText().toString() : "";

                    //  Check if row matches selected filters
                    boolean matchesSupplier = selectedSupplier.equals("All") || supplierValue.equals(selectedSupplier);
                    boolean matchesStatus = selectedStatus.equals("All") || statusValue.equals(selectedStatus);

                    //  Show/Hide row based on filter match
                    row.setVisibility((matchesSupplier && matchesStatus) ? View.VISIBLE : View.GONE);
                } else {
                    Log.e("Filter", "Row does not have enough children!");
                }
            }
        }
        Log.d("Filter", "Selected Supplier: " + selectedSupplier);
        Log.d("Filter", "Selected Status: " + selectedStatus);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                // Formatting the selected date
                String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay ;
                txtetdate.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void exportStockHistoryToPDF() {
        Rectangle shortSize = new Rectangle(612, 792); // Width x Height in points (8.5 x 11 inches)
        Document document = new Document(shortSize, 40, 40, 50, 50);
        try {

            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            String fileName = "Stock History_" + currentDateAndTime + ".pdf";
            File file = new File(pdfPath, fileName);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // 1. Add Logo
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logor); // your logo file
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image logo = Image.getInstance(stream.toByteArray());
            logo.scaleToFit(100, 100);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);

            // 2. Add Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Stock History\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            // 3. Meta Table (Printed by / Date)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1, 1});
            metaTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);


            String role = AuthManager.Role;
            String supplier = AuthManager.Supplier;
            String user = "";

            switch (role) {
                case "Admin":
                case "Employee":
                    user = role;
                    break;
                case "Diser":
                    user = supplier + " " + role;
                    break;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            PdfPCell printedByCell = new PdfPCell(new Phrase("Printed by: " + user, labelFont));
            printedByCell.setBorder(PdfPCell.NO_BORDER);
            printedByCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            metaTable.addCell(printedByCell);

            PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + currentDate, labelFont));
            dateCell.setBorder(PdfPCell.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            metaTable.addCell(dateCell);



            metaTable.setSpacingAfter(15f);
            document.add(metaTable);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{4, 4});
            summaryTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

            // Row 1
            PdfPCell returnCell = new PdfPCell(new Phrase("Supplier: " + supplierSpinner.getSelectedItem().toString(), labelFont));
            returnCell.setBorder(PdfPCell.NO_BORDER);
            returnCell.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell totalSaleCell = new PdfPCell(new Phrase("Status: " + statusSpinner.getSelectedItem().toString(), labelFont));
            totalSaleCell.setBorder(PdfPCell.NO_BORDER);
            totalSaleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            summaryTable.addCell(returnCell);
            summaryTable.addCell(totalSaleCell);

            summaryTable.setSpacingAfter(20f);
            document.add(summaryTable);


            // 4. Table with styled header
            int columnCount = 10; // Adjust if your table has more/less
            PdfPTable pdfTable = new PdfPTable(columnCount);
            pdfTable.setWidthPercentage(100);

            // Extract headers from first row
            TableRow headerRow = (TableRow) tableLayout.getChildAt(0);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, BaseColor.WHITE);
            for (int i = 0; i < columnCount; i++) {
                TextView headerCell = (TextView) headerRow.getChildAt(i);
                PdfPCell cell = new PdfPCell(new Phrase(headerCell.getText().toString(), headerFont));
                cell.setBackgroundColor(new BaseColor(0x05, 0x26, 0x85));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6f);
                pdfTable.addCell(cell);
            }

            // Add only visible rows
            Font rowFont = new Font(Font.FontFamily.HELVETICA, 8);
            for (int i = 1; i < tableLayout.getChildCount(); i++) {
                TableRow row = (TableRow) tableLayout.getChildAt(i);
                if (row.getVisibility() == View.VISIBLE) {
                    for (int j = 0; j < columnCount; j++) {
                        TextView cell = (TextView) row.getChildAt(j);
                        PdfPCell pdfCell = new PdfPCell(new Phrase(cell.getText().toString(), rowFont));
                        pdfCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Center alignment
                        pdfTable.addCell(pdfCell);
                    }
                }
            }

            document.add(pdfTable);
            document.close();

            Toast.makeText(requireContext(), "PDF saved to: " + pdfPath, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
