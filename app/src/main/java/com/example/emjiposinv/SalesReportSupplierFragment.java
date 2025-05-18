package com.example.emjiposinv;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

public class SalesReportSupplierFragment extends Fragment {

    private TextView txtReturn, txtDiscount, txtNetSale, txtTotalSale;
    private DatabaseHelper databaseHelper;
    private Spinner yearSpinner, monthSpinner, supplierSpinner;

    private EditText dailyDate;
    private Button btnSavetoPDF;

    private TableLayout tableLayout;
    private SupabaseAuthApi supabaseAuthApi;

    private List<String> supplierList = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_sales_report_supplier, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        databaseHelper = new DatabaseHelper(requireContext());
        yearSpinner = view.findViewById(R.id.ddYear);
        monthSpinner = view.findViewById(R.id.ddmonth);
        supplierSpinner = view.findViewById(R.id.ddsupplier2);
        setupSpinner(supplierSpinner, getSupplierList());
        fetchData();


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        txtReturn = view.findViewById(R.id.txtreturn);
        txtDiscount = view.findViewById(R.id.txtdiscount);
        txtNetSale = view.findViewById(R.id.txtnetsale);
        txtTotalSale = view.findViewById(R.id.txttotalsale);

        btnSavetoPDF = view.findViewById(R.id.btnsavepdf);
        btnSavetoPDF.setOnClickListener(v -> exportVisibleRowsToPDF());



        // Initialize DatePicker EditText
        dailyDate = view.findViewById(R.id.etDate4);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        dailyDate.setText(currentDate);
        dailyDate.setOnClickListener(v -> showDatePicker());

// Year Spinner Setup
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        years.add("Select Year"); // Hint item
        for (int i = currentYear; i >= 2000; i--) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, years);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setSelection(0); // Default to "Select Year"

// Month Spinner Setup
        List<String> months = new ArrayList<>();
        months.add("Select Month"); // Hint item
        months.addAll(Arrays.asList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, months);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(0); // Default to "Select Month"

// Year Selection Listener
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    dailyDate.setText(currentDate);
                    filterTableByDate();
                    return;
                } // Ignore "Select Year"
                String selectedYear = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected Year: " + selectedYear, Toast.LENGTH_SHORT).show();
                dailyDate.setText("");
                filterTableBySpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

// Month Selection Listener
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    dailyDate.setText(currentDate);
                    filterTableByDate();
                    return;
                } // Ignore "Select Month"
                String selectedMonth = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected Month: " + selectedMonth, Toast.LENGTH_SHORT).show();
                dailyDate.setText("");
                filterTableBySpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        supplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 ) {
                    if (dailyDate.getText().toString().isEmpty()){
                        dailyDate.setText(currentDate);
                    }
                    filterTableByDate();
                    return;
                }
                String selectedSupplier = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected Supplier: " + selectedSupplier, Toast.LENGTH_SHORT).show();
                filterTableBySpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


    }

    private void filterTableBySpinners() {
        String selectedYear = yearSpinner.getSelectedItem().toString();
        String selectedMonth = monthSpinner.getSelectedItem().toString();
        String selectedSupplier = supplierSpinner.getSelectedItem().toString();
        String selectedDate = dailyDate.getText().toString().trim();

        boolean isDailyDateSet = !selectedDate.isEmpty();

        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            TextView dateTextView = (TextView) row.getChildAt(9);
            TextView supplierTextView = (TextView) row.getChildAt(8);

            String rawDate = dateTextView.getText().toString().trim();
            String rowSupplier = supplierTextView.getText().toString().trim();

            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = dbFormat.parse(rawDate);

                SimpleDateFormat compareFormat = new SimpleDateFormat("yyyy/M/d", Locale.getDefault());
                String formattedDate = compareFormat.format(date);

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                String rowYear = String.valueOf(cal.get(Calendar.YEAR));
                String rowMonth = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

                if (isDailyDateSet) {
                    // Filter using only dailyDate + supplier
                    boolean matchDate = formattedDate.equals(selectedDate);
                    boolean matchSupplier = selectedSupplier.equals("Select Supplier") || rowSupplier.equalsIgnoreCase(selectedSupplier);

                    row.setVisibility(matchDate && matchSupplier ? View.VISIBLE : View.GONE);
                } else {
                    // Filter using year + month + supplier
                    boolean matchYear = selectedYear.equals("Select Year") || rowYear.equals(selectedYear);
                    boolean matchMonth = selectedMonth.equals("Select Month") || rowMonth.equalsIgnoreCase(selectedMonth);
                    boolean matchSupplier = selectedSupplier.equals("Select Supplier") || rowSupplier.equalsIgnoreCase(selectedSupplier);

                    row.setVisibility(matchYear && matchMonth && matchSupplier ? View.VISIBLE : View.GONE);
                }

            } catch (ParseException e) {
                e.printStackTrace();
                row.setVisibility(View.GONE);
            }
        }

        prepareAndFetchSummaryData();
    }








    private void filterTableByDate() {
        String selectedDate = dailyDate.getText().toString().trim();

        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            TextView dateTextView = (TextView) row.getChildAt(9);
            String rawDate = dateTextView.getText().toString().trim();

            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = dbFormat.parse(rawDate);

                SimpleDateFormat compareFormat = new SimpleDateFormat("yyyy/M/d", Locale.getDefault());
                String formattedDate = compareFormat.format(date);

                boolean isMatch = formattedDate.equals(selectedDate);
                row.setVisibility(isMatch ? View.VISIBLE : View.GONE);

            } catch (ParseException e) {
                e.printStackTrace();
                row.setVisibility(View.GONE);
            }
        }

        supplierSpinner.setSelection(0);
        yearSpinner.setSelection(0);
        monthSpinner.setSelection(0);
        prepareAndFetchSummaryData();
    }


    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            // Format selected date as yyyy/M/d
            String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
            dailyDate.setText(selectedDate);

            // Call filter after setting the new date
            filterTableByDate();
        }, year, month, day);

        datePickerDialog.show();
    }

    // Setup Dropdowns
    private void setupSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
    }

    private List<String> getSupplierList() {
        String supplier = AuthManager.Supplier;
        List<String> items = new ArrayList<>();
        items.add(supplier);
        return items;
    }

    private void fetchData() {
        supabaseAuthApi.getAllSalesReport(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<SalesReport>>() {
            @Override
            public void onResponse(Call<List<SalesReport>> call, Response<List<SalesReport>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                    filterTableBySpinners();
                } else {
                    Log.e("Supabase", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<SalesReport>> call, Throwable t) {
                Log.e("Supabase", "Failed to fetch data", t);
            }
        });
    }

    private void populateTable(List<SalesReport> salesreports) {
        tableLayout.removeAllViews();
        addTableHeader();

        for (SalesReport salesreport : salesreports) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView TransactionNumberText = createStyledCell(String.valueOf(salesreport.getTransactionNumber()));
            TextView ProductIdText = createStyledCell(String.valueOf(salesreport.getProductId()));
            TextView BatchNumberText = createStyledCell(String.valueOf(salesreport.getBatchNumber()));
            TextView ProductNameText = createStyledCell(salesreport.getProductName());
            TextView quantityText = createStyledCell(salesreport.getQuantity());
            TextView PurchasePriceText = createStyledCell(String.valueOf(salesreport.getPurchasePrice()));
            TextView UnitPriceText = createStyledCell(String.valueOf(salesreport.getUnitPrice()));
            TextView TotalPriceText = createStyledCell(String.valueOf(salesreport.getTotalPrice()));
            TextView SupplierText = createStyledCell(salesreport.getSupplier());
            TextView DateText = createStyledCell(salesreport.getDate());


            row.addView(TransactionNumberText);
            row.addView(ProductIdText);
            row.addView(BatchNumberText);
            row.addView(ProductNameText);
            row.addView(quantityText);
            row.addView(PurchasePriceText);
            row.addView(UnitPriceText);
            row.addView(TotalPriceText);
            row.addView(SupplierText);
            row.addView(DateText);

            tableLayout.addView(row);

            filterTableByDate();
        }
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue));

        String[] headers = {"Transaction Number", "Product ID", "Batch Number", "Product Name", "Quantity", "Purchase Price", "Unit Price", "Total Price", "Supplier", "Date"};
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
    private void prepareAndFetchSummaryData() {
        Set<String> transactionNumbers = new HashSet<>();
        List<Map<String, String>> returnKeys = new ArrayList<>();

        int visibleRowCount = 0; // Count visible rows only

        // Loop through each row in the table
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            if (row.getVisibility() != View.VISIBLE) {
                continue; // Skip hidden rows
            }

            visibleRowCount++; // Count visible

            String transactionNumber = ((TextView) row.getChildAt(0)).getText().toString().trim();
            String productId = ((TextView) row.getChildAt(1)).getText().toString().trim();
            String batchNumber = ((TextView) row.getChildAt(2)).getText().toString().trim();

            transactionNumbers.add(transactionNumber);

            Map<String, String> key = new HashMap<>();
            key.put("transactionNumber", transactionNumber);
            key.put("productId", productId);
            key.put("batchNumber", batchNumber);
            returnKeys.add(key);
        }

        // ✅ If no visible rows, reset totals and stop
        if (visibleRowCount == 0) {
            NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            txtReturn.setText(pesoFormat.format(0.00));
            txtDiscount.setText(pesoFormat.format(0.00));
            txtNetSale.setText(pesoFormat.format(0.00));
            txtTotalSale.setText(pesoFormat.format(0.00));
            return;
        }

        // ✅ Continue fetching summary only if visible rows exist
        databaseHelper.getFilteredReturns(returnKeys, new ReturnListCallback() {
            @Override
            public void onSuccess(List<ReturnItem> returnList) {
                databaseHelper.getFilteredTransactionHistory(transactionNumbers, new TransactionHistoryCallback() {
                    @Override
                    public void onSuccess(List<TransacHis> historyList) {
                        computeAndDisplaySalesSummary(returnList, historyList);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("Summary", error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("Summary", error);
            }
        });
    }

    private void computeAndDisplaySalesSummary(List<ReturnItem> returnList, List<TransacHis> historyList) {
        double totalReturn = 0.0;
        double totalDiscount = 0.0;
        double totalNetSale = 0.0;
        double totalSale = 0.0;

        Set<String> processedTransactionNumbers = new HashSet<>();

// Loop through each row in the sales table
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);

            // Skip hidden rows
            if (row.getVisibility() != View.VISIBLE) {
                continue; // Skip this iteration if the row is not visible
            }

            // Parse values only for visible rows
            String transactionNumber = ((TextView) row.getChildAt(0)).getText().toString().trim();
            String productId = ((TextView) row.getChildAt(1)).getText().toString().trim();
            String batchNumber = ((TextView) row.getChildAt(2)).getText().toString().trim();
            int quantity = 0;
            double purchasePrice = 0.0, unitPrice = 0.0, totalPrice = 0.0;

            try {
                quantity = Integer.parseInt(((TextView) row.getChildAt(4)).getText().toString().trim());
                purchasePrice = Double.parseDouble(((TextView) row.getChildAt(5)).getText().toString().trim());
                unitPrice = Double.parseDouble(((TextView) row.getChildAt(6)).getText().toString().trim());
                totalPrice = Double.parseDouble(((TextView) row.getChildAt(7)).getText().toString().trim());
            } catch (NumberFormatException e) {
                Log.e("SalesReportFragment", "Invalid number format in row " + i);
            }

            // Log values for debugging
            Log.d("SalesReportFragment", "Processing row " + i + ": " +
                    "Transaction Number: " + transactionNumber + ", " +
                    "Product ID: " + productId + ", " +
                    "Batch Number: " + batchNumber);

            // Compute Return for this row
            for (ReturnItem ret : returnList) {
                if (ret != null) {
                    // Log the return item details for debugging
                    Log.d("SalesReportFragment", "Checking return item: " +
                            "Transaction Number: " + ret.getTransactionNumber() + ", " +
                            "Product ID: " + ret.getProductId() + ", " +
                            "Batch Number: " + ret.getBatchNumber());

                    // Ensure the values are not null before calling equals
                    if (ret.getTransactionNumber() != null &&
                            ret.getProductId() != null &&
                            ret.getBatchNumber() != null &&
                            ret.getTransactionNumber().equals(transactionNumber) &&
                            ret.getProductId().equals(productId) &&
                            ret.getBatchNumber().equals(batchNumber)) {
                        totalReturn += unitPrice * ret.getQuantity();
                    }
                }
            }

            // Compute Discount (only once per unique transaction number)
            if (!processedTransactionNumbers.contains(transactionNumber)) {
                for (TransacHis hist : historyList) {
                    if (hist != null && hist.getTransactionNumber() != null &&
                            hist.getTransactionNumber().equals(transactionNumber)) {
                        totalDiscount += hist.getDiscount();
                        Log.d("SalesReportFragment","Discount: " + totalDiscount);
                        break; // Only process the first matching transaction for discount
                    }
                }
                processedTransactionNumbers.add(transactionNumber);
            }

            // Compute Net Sale (after subtracting purchase price from total sale price)
            double netSale = totalPrice - (quantity * purchasePrice);
            totalNetSale += netSale;

            // Compute Total Sale (before returns)
            totalSale += totalPrice;
        }

        // Adjust total sale after considering returns
        totalSale -= totalReturn + totalDiscount;

        // Display results to the respective TextViews
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

        txtReturn.setText(pesoFormat.format(totalReturn));
        txtDiscount.setText(pesoFormat.format(totalDiscount));
        txtNetSale.setText(pesoFormat.format(totalNetSale));
        txtTotalSale.setText(pesoFormat.format(totalSale));
    }

    private void exportVisibleRowsToPDF() {
        Rectangle shortSize = new Rectangle(612, 792); // Width x Height in points (8.5 x 11 inches)
        Document document = new Document(shortSize, 40, 40, 50, 50);
        try {

            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            String fileName = "SalesReport_" + currentDateAndTime + ".pdf";
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
            Paragraph title = new Paragraph("Sales Report\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // 3. Printed by and Date
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            // 1. Meta Table (Printed by / Date)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1, 1});
            metaTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);



            String selectedSupplier = supplierSpinner.getSelectedItem().toString();
            String selectedYear = yearSpinner.getSelectedItem().toString();
            String selectedMonth = monthSpinner.getSelectedItem().toString();
            String date = dailyDate.getText().toString();

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

            if (!date.isEmpty()) {

                PdfPCell printedByCell = new PdfPCell(new Phrase("Printed by: " + user, labelFont));
                printedByCell.setBorder(PdfPCell.NO_BORDER);
                printedByCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                metaTable.addCell(printedByCell);

                PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + date, labelFont));
                dateCell.setBorder(PdfPCell.NO_BORDER);
                dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                metaTable.addCell(dateCell);
            } else {
                PdfPCell printedByCell = new PdfPCell(new Phrase("Printed by: " + role, labelFont));
                printedByCell.setBorder(PdfPCell.NO_BORDER);
                printedByCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                metaTable.addCell(printedByCell);
                if (selectedMonth.equals("Select Month")) {
                    PdfPCell YearMonthCell = new PdfPCell(new Phrase("Year: " + selectedYear, labelFont));
                    YearMonthCell.setBorder(PdfPCell.NO_BORDER);
                    YearMonthCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    metaTable.addCell(YearMonthCell);
                } else if (selectedYear.equals("Select Year")) {
                    PdfPCell YearMonthCell = new PdfPCell(new Phrase("Month: " + selectedMonth, labelFont));
                    YearMonthCell.setBorder(PdfPCell.NO_BORDER);
                    YearMonthCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    metaTable.addCell(YearMonthCell);
                }else {
                    PdfPCell YearMonthCell = new PdfPCell(new Phrase("Month/Year: " + selectedMonth + " " + selectedYear, labelFont));
                    YearMonthCell.setBorder(PdfPCell.NO_BORDER);
                    YearMonthCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    metaTable.addCell(YearMonthCell);
                }
            }


            metaTable.setSpacingAfter(15f);
            document.add(metaTable);

            PdfPTable suppTable = new PdfPTable(2);
            suppTable.setWidthPercentage(100);
            suppTable.setWidths(new float[]{1, 1});
            suppTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

            if (!date.isEmpty()) {
                if (!selectedSupplier.equals("Select Supplier")) {
                    PdfPCell supplierCell = new PdfPCell(new Phrase("Supplier: " + selectedSupplier, labelFont));
                    supplierCell.setBorder(PdfPCell.NO_BORDER);
                    supplierCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    suppTable.addCell(supplierCell);

                    // Add empty cell to complete the row
                    PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                    emptyCell.setBorder(PdfPCell.NO_BORDER);
                    suppTable.addCell(emptyCell);
                }
            } else {
                if (!selectedMonth.equals("Select Month") || !selectedYear.equals("Select Year")) {
                    if (!selectedSupplier.equals("Select Supplier")) {
                        PdfPCell supplierCell = new PdfPCell(new Phrase("Supplier: " + selectedSupplier, labelFont));
                        supplierCell.setBorder(PdfPCell.NO_BORDER);
                        supplierCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        suppTable.addCell(supplierCell);

                        // Add empty cell to complete the row
                        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                        emptyCell.setBorder(PdfPCell.NO_BORDER);
                        suppTable.addCell(emptyCell);
                    }
                }
            }

            suppTable.setSpacingAfter(15f);
            document.add(suppTable);



            // 2. Summary Section (Return, Discount, Total Sale, Net Sale)
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{4, 4});
            summaryTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

            // Row 1
            PdfPCell returnCell = new PdfPCell(new Phrase("Return: " + txtReturn.getText().toString(), labelFont));
            returnCell.setBorder(PdfPCell.NO_BORDER);
            returnCell.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell totalSaleCell = new PdfPCell(new Phrase("Gross Sale: " + txtTotalSale.getText().toString(), labelFont));
            totalSaleCell.setBorder(PdfPCell.NO_BORDER);
            totalSaleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            summaryTable.addCell(returnCell);
            summaryTable.addCell(totalSaleCell);

// Row 2
            PdfPCell discountCell = new PdfPCell(new Phrase("Discount: " + txtDiscount.getText().toString(), labelFont));
            discountCell.setBorder(PdfPCell.NO_BORDER);
            discountCell.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell netSaleCell = new PdfPCell(new Phrase("Net Sale: " + txtNetSale.getText().toString(), labelFont));
            netSaleCell.setBorder(PdfPCell.NO_BORDER);
            netSaleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            summaryTable.addCell(discountCell);
            summaryTable.addCell(netSaleCell);

            summaryTable.setSpacingAfter(20f);
            document.add(summaryTable);

            // 5. Table with styled header
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

