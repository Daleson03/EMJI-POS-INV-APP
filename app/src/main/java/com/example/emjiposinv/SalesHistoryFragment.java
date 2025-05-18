package com.example.emjiposinv;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SalesHistoryFragment extends Fragment {
    private TableLayout tableLayout;
    private SupabaseAuthApi supabaseAuthApi;

    private Button btnScan;

    private EditText txtTrans;

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
        View view = inflater.inflate(R.layout.fragment_sales_history, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        txtTrans = view.findViewById(R.id.txtproductname3);
        btnScan = view.findViewById(R.id.btnscan5);

        btnScan.setOnClickListener(v -> launchBarcodeScanner());

        txtTrans.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTable(tableLayout, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchData();


        return view;
    }

    private void filterTable(TableLayout tableLayout, String query) {
        int rowCount = tableLayout.getChildCount();
        Log.d("Filter", "Total Rows: " + rowCount); //  Debugging log

        for (int i = 1; i < rowCount; i++) { // Start from 1 to skip header row
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                TextView ProductIDView = (TextView) row.getChildAt(0); //  Correct column index

                if (ProductIDView != null) {
                    String ProductID = ProductIDView.getText().toString().toLowerCase();
                    if (ProductID.contains(query.toLowerCase())) {
                        row.setVisibility(View.VISIBLE);
                    } else {
                        row.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    txtTrans.setText(result.getContents()); // Set scanned barcode to EditText
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

    private void fetchData() {
        supabaseAuthApi.getAllTransactionHistory(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<TransactionHistory>>() {
            @Override
            public void onResponse(Call<List<TransactionHistory>> call, Response<List<TransactionHistory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                } else {
                    Log.e("Supabase", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<TransactionHistory>> call, Throwable t) {
                Log.e("Supabase", "Failed to fetch data", t);
            }
        });
    }

    private void populateTable(List<TransactionHistory> transactionhistorys) {
        tableLayout.removeAllViews();
        addTableHeader();

        for (TransactionHistory transactionhistory : transactionhistorys) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


            TextView TransactionNumberText = createStyledCell(transactionhistory.getTransactionNumber());
            TextView SubtotalText = createStyledCell(String.valueOf(transactionhistory.getSubtotal()));
            TextView DiscountText = createStyledCell(String.valueOf(transactionhistory.getDiscount()));
            TextView TotalAmountText = createStyledCell(String.valueOf(transactionhistory.getTotalAmount()));
            TextView ChangeText = createStyledCell(String.valueOf(transactionhistory.getChange()));
            TextView DateText = createStyledCell(transactionhistory.getDate());
            TextView PaymentMethodText = createStyledCell(transactionhistory.getPaymentMethod());
            TextView CashText = createStyledCell(String.valueOf(transactionhistory.getCash()));
            TextView ReferenceNumberText = createStyledCell(transactionhistory.getReferenceNumber());


            row.addView(TransactionNumberText);
            row.addView(SubtotalText);
            row.addView(DiscountText);
            row.addView(TotalAmountText);
            row.addView(ChangeText);
            row.addView(DateText);
            row.addView(PaymentMethodText);
            row.addView(CashText);
            row.addView(ReferenceNumberText);

            tableLayout.addView(row);
        }
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue));

        String[] headers = {"Transaction Number", "Subtotal", "Discount", "Total Amount", "Change", "Date", "Payment Method", "Cash", "Reference Number"};
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
}