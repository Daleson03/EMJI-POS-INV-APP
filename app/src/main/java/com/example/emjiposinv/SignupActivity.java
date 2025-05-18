package com.example.emjiposinv;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
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
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SignupActivity extends AppCompatActivity {
    private AuthManager authManager;

    private String Roles, Suppliers;
    private Spinner supplierSpinner, roleSpinner;

    private TextView  txtsupplier;


    private TableLayout tableLayout;
    private EditText email, password;

    private List<String> supplierList = new ArrayList<>();

    private SupabaseAuthApi supabaseAuthApi;




    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJ" +
            "lZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3N" +
            "TcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhY" +
            "mFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTc" +
            "xNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        authManager = new AuthManager(this);

        tableLayout = findViewById(R.id.tableLayout);
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        Button signupButton = findViewById(R.id.signupButton);




        txtsupplier = findViewById(R.id.txtsupplier);


        supplierSpinner = findViewById(R.id.ddsupplier);
        roleSpinner = findViewById(R.id.ddrole);

        //  Setup Dropdowns (Spinners)
        setupSpinner(roleSpinner, getRoleList());

        hideViews(txtsupplier, supplierSpinner);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Check if the selected item is "Diser"
                if (roleSpinner.getSelectedItem().equals("Diser")) {
                    showViews(txtsupplier, supplierSpinner);
                    fetchSupplierstoSpinner();
                } else {
                    hideViews(txtsupplier, supplierSpinner);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optional: handle case when no item is selected, if needed
            }
        });

        // Sign Up Button Click
        signupButton.setOnClickListener(v -> {
            String userEmail = email.getText().toString();
            String userPassword = password.getText().toString();
            String spinRole = roleSpinner.getSelectedItem().toString();


            if (spinRole.equals("Select Role")) {
                Toast.makeText(SignupActivity.this, "Please select a role!", Toast.LENGTH_SHORT).show();
                return;
            }

            Roles = roleSpinner.getSelectedItem().toString();


            if (spinRole.equals("Diser")) {
                String spinSup = supplierSpinner.getSelectedItem().toString();
                if (spinSup.equals("Select Supplier")) {
                    Toast.makeText(SignupActivity.this, "Please select a valid supplier!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Suppliers = supplierSpinner.getSelectedItem().toString();
                }
            }else {
                Suppliers = "";
            }


            if (!userEmail.isEmpty() && !userPassword.isEmpty()) {
                authManager.signUpUser(userEmail, userPassword);
                insertAccount();
            } else {
                Toast.makeText(SignupActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            }
        });

        fetchData();

    }


    private void fetchSupplierstoSpinner() {
        supabaseAuthApi.getSupplierstoSpinner(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    supplierList.clear();
                    supplierList.add("Select Supplier"); // Add default option

                    for (Supplier supplier : response.body()) {
                        supplierList.add(supplier.getSupplierName());
                    }
                    populateSpinner();
                } else {
                    Log.e("Supabase", "Failed to fetch suppliers: " + response.code() + " - " + response.message());
                    Toast.makeText(SignupActivity.this, "Failed to fetch suppliers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  Populate Supplier Spinner
    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SignupActivity.this, R.layout.spinner_item, supplierList);
        supplierSpinner.setAdapter(adapter);
    }

    private void setupSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SignupActivity.this, R.layout.spinner_item, items);
        spinner.setAdapter(adapter);
    }



    private List<String> getRoleList() {
        List<String> items = new ArrayList<>();
        items.add("Select Role");
        items.add("Admin");
        items.add("Diser");
        items.add("Employee");
        return items;
    }

    public void insertAccount() {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();



        // Prepare the accout data
        Map<String, Object> account = new HashMap<>();
        account.put("email", userEmail);
        account.put("password", userPassword);
        account.put("roles", Roles);
        account.put("supplier", Suppliers);


        // Insert into the database via the API
        supabaseAuthApi.insertaccount(account).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Account Inserted Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API Error", errorBody);  // Log the error body for debugging
                        Toast.makeText(SignupActivity.this, "Insert Failed: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchData() {
        supabaseAuthApi.getAllAccountData(API_KEY, AUTH_TOKEN).enqueue(new Callback<List<Account>>() {
            @Override
            public void onResponse(Call<List<Account>> call, Response<List<Account>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTable(response.body());
                } else {
                    Log.e("Supabase", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Account>> call, Throwable t) {
                Log.e("Supabase", "Failed to fetch data", t);
            }
        });
    }

    private void populateTable(List<Account> accounts) {
        tableLayout.removeAllViews();
        addTableHeader();

        for (Account account : accounts) {
            TableRow row = new TableRow(SignupActivity.this);
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView EmailText = createStyledCell(account.getEmail());
            TextView PasswordText = createStyledCell(maskPassword(account.getPassword()));
            TextView RoleText = createStyledCell(account.getRoles());

            // Toggle Password Button
            TextView btnTogglePassword = new TextView(SignupActivity.this);
            btnTogglePassword.setText("Show");
            btnTogglePassword.setTextSize(14);
            btnTogglePassword.setPadding(12, 8, 12, 8);
            btnTogglePassword.setGravity(Gravity.CENTER);
            btnTogglePassword.setTypeface(null, Typeface.BOLD);
            btnTogglePassword.setTextColor(getResources().getColor(R.color.white));
            btnTogglePassword.setBackgroundResource(R.drawable.toggle_password_button);

            btnTogglePassword.setOnClickListener(v -> {
                if (btnTogglePassword.getText().toString().equals("Show")) {
                    PasswordText.setText(account.getPassword());
                    btnTogglePassword.setText("Hide");
                } else {
                    PasswordText.setText(maskPassword(account.getPassword()));
                    btnTogglePassword.setText("Show");
                }
            });

            // Delete Button

            // Add all views to the row
            row.addView(EmailText);
            row.addView(PasswordText);
            row.addView(RoleText);
            row.addView(btnTogglePassword);

            tableLayout.addView(row);
        }
    }






    private void addTableHeader() {
        TableRow headerRow = new TableRow(SignupActivity.this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(ContextCompat.getColor(SignupActivity.this, R.color.blue));

        String[] headers = {"Email", "Password", "Roles", "Action"};
        for (String header : headers) {
            TextView textView = createStyledHeader(header);
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    private TextView createStyledCell(String text) {
        TextView textView = new TextView(SignupActivity.this);
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
        TextView textView = new TextView(SignupActivity.this);
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        return textView;
    }

    private String maskPassword(String password) {
        if (password == null) return "";
        return new String(new char[password.length()]).replace('\0', '*');
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