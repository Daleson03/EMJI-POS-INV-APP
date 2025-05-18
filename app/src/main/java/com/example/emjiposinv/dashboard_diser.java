package com.example.emjiposinv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class dashboard_diser extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    private SupabaseAuthApi supabaseAuthApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_diser);

        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        // Only set toolbar if no action bar is already provided by the window decor
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle back button behavior using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    showExitConfirmationDialog();
                }
            }
        });

        // Access the NavigationView header and set the welcome text
        View headerView = navigationView.getHeaderView(0); // Get the first header
        TextView welcometext = headerView.findViewById(R.id.welcometext); // Find the TextView inside the header

        // Set the welcome text dynamically
        String role = AuthManager.Role;// Assuming this value is set earlier
        String supplier = AuthManager.Supplier;

        switch (role) {
            case "Admin":
                welcometext.setText("Welcome " + role);
                break;
            case "Employee":
                welcometext.setText("Welcome " + role);
                break;
            case "Diser":
                welcometext.setText("             Welcome \n" + "           " + supplier + " " + role);
                welcometext.setTextSize(15);
                break;
        }
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_dashboard) {
            selectedFragment = new DashboardFragment();
        } else if (itemId == R.id.nav_sales_report) {
            selectedFragment = new SalesReportSupplierFragment();
        } else if (itemId == R.id.nav_sales_history) {
            selectedFragment = new SalesHistoryFragment();
        } else if (itemId == R.id.nav_new_purchase) {
            selectedFragment = new NewPurchaseFragment();
        } else if (itemId == R.id.nav_return_purchase) {
            selectedFragment = new ReturnPurchaseFragment();
        } else if (itemId == R.id.nav_add_update) {
            selectedFragment = new AddUpdateFragment();
        } else if (itemId == R.id.nav_product_list) {
            selectedFragment = new ProductListSupplierFragment();
        } else if (itemId == R.id.nav_manage_stock) {
            selectedFragment = new ManageStockFragment();
        } else if (itemId == R.id.nav_stock_history) {
            selectedFragment = new StockHistorySupplierFragment();
        } else if (itemId == R.id.nav_add_update_sup) {
            selectedFragment = new AddUpdateSupFragment();
        } else if (itemId == R.id.nav_supplier_list) {
            selectedFragment = new SupplierListFragment();
        } else if (itemId == R.id.nav_setting) {
            selectedFragment = new SettingFragment();
        } else if (itemId == R.id.nav_logout) {
            showLogoutDialog();
            return true; // Stop further execution after logout
        }

        // ✅ Replace fragment if one is selected
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        // ✅ Set the checked state properly
        item.setChecked(true);

        // ✅ Close the navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }










    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> logoutUser());

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logoutUser() {
        Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(dashboard_diser.this, MainActivity.class);
        startActivity(intent);
        logUserAction();
        finish();
    }

    public void logUserAction() {
        String email = AuthManager.emails;
        String role = AuthManager.Role;// Assuming this value is set earlier
        String supplier = AuthManager.Supplier;

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String action = "Log Out";

        Map<String, Object> logData = new HashMap<>();
        logData.put("email", email);

        String user;
        if (role.equals("Diser")) {
            user = supplier + " " + role;
            logData.put("user", user);
        } else {
            logData.put("user", role);
        }
        logData.put("date", currentDate);
        logData.put("time", currentTime);
        logData.put("action", action);  // "Log In" or "Log Out"

        supabaseAuthApi.insertAccountLog(logData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("LogAction", "User action logged successfully.");
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("LogAction", "Insert failed: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("LogAction", "Error logging action: " + t.getMessage());
            }
        });
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> logoutUser());

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
