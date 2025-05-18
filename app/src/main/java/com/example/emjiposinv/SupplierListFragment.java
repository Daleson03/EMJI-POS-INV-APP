package com.example.emjiposinv;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupplierListFragment extends Fragment {

    private TableLayout tableLayout;
    private SupabaseAuthApi supabaseAuthApi;
    private Button btnAddSup;
    private EditText searchSupplier;

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIs" +
            "InJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA" +
            "1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdX" +
            "BhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczO" +
            "TA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_supplier_list, container, false);

        btnAddSup = view.findViewById(R.id.btnaddsup);
        searchSupplier = view.findViewById(R.id.txtsuppliern);
        tableLayout = view.findViewById(R.id.tableLayout);

        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        btnAddSup.setOnClickListener(v -> navigateToAddUpdateSupFragment());

        fetchData(); // Fetch data when the fragment loads

        searchSupplier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTable(tableLayout, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void navigateToAddUpdateSupFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, new AddUpdateSupFragment()); // Ensure this ID exists in your layout
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void filterTable(TableLayout tableLayout, String query) {
        int rowCount = tableLayout.getChildCount();
        Log.d("Filter", "Total Rows: " + rowCount); // Debugging log

        for (int i = 1; i < rowCount; i++) { // Start from 1 to skip header row
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                TextView supplierNameView = (TextView) row.getChildAt(1); //Correct column index

                if (supplierNameView != null) {
                    String supplierName = supplierNameView.getText().toString().toLowerCase();
                    if (supplierName.contains(query.toLowerCase())) {
                        row.setVisibility(View.VISIBLE);
                    } else {
                        row.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void fetchData() {
        supabaseAuthApi.getAllSupplier(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                } else {
                    Log.e("Supabase", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                Log.e("Supabase", "Failed to fetch data", t);
            }
        });
    }

    private void populateTable(List<Supplier> suppliers) {
        tableLayout.removeAllViews();
        addTableHeader();

        for (Supplier supplier : suppliers) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            TextView idText = createStyledCell(String.valueOf(supplier.getSupplierID()));
            TextView nameText = createStyledCell(supplier.getSupplierName());
            TextView contactText = createStyledCell(supplier.getContactPerson());
            TextView phoneText = createStyledCell(String.valueOf(supplier.getPhone()));
            TextView emailText = createStyledCell(String.valueOf(supplier.getEmail()));
            TextView timeText = createStyledCell(supplier.getCutoffTime());

            row.addView(idText);
            row.addView(nameText);
            row.addView(contactText);
            row.addView(phoneText);
            row.addView(emailText);
            row.addView(timeText);

            tableLayout.addView(row);
        }
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue));

        String[] headers = {"Supplier ID", "Supplier Name", "Contact Person", "Phone", "Email", "Cutoff Time"};
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
