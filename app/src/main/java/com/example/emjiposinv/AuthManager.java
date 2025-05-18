package com.example.emjiposinv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AuthManager {


    private final SupabaseAuthApi api;

    private final Context context;
    public static String Role;
    public static String Supplier;

    public static String emails;

    public AuthManager(Context context) {
        this.api = RetrofitClient.getSupabaseApi();
        this.context = context;
    }

    public void signUpUser(String email, String password) {
        Map<String, String> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);


        api.signUp(user).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(context, MainActivity.class);
                    //context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Sign-Up Failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Login
    public void signInUser(String email, String password) {
        Map<String, String> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);

        emails = email;

        api.signIn(user).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Now fetch the AccountTB info from Supabase
                    fetchUserRoleAndRedirect(email);
                } else {
                    Toast.makeText(context, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserRoleAndRedirect(String email) {
        api.getAccountByEmail("eq." + email).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> userData = response.body().get(0);
                    String role = (String) userData.get("roles");
                    String supplier = (String) userData.get("supplier");

                    AuthManager.Role = role;
                    AuthManager.Supplier = supplier;

                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent;
                    switch (role.toLowerCase()) {
                        case "admin":
                            intent = new Intent(context, dashboard.class);
                            logUserAction();
                            break;
                        case "employee":
                            intent = new Intent(context, dashboard_employee.class);
                            logUserAction();
                            break;
                        case "diser":
                            intent = new Intent(context, dashboard_diser.class);
                            logUserAction();
                            break;
                        default:
                            Toast.makeText(context, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                            return;
                    }

                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "User data not found in AccountTB!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void logUserAction() {


        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String action = "Log In";

        Map<String, Object> logData = new HashMap<>();
        logData.put("email", emails);

        String user;
        if (Role.equals("Diser")) {
            user = Supplier + " " + Role;
            logData.put("user", user);
        } else {
            logData.put("user", Role);
        }
        logData.put("date", currentDate);
        logData.put("time", currentTime);
        logData.put("action", action);  // "Log In" or "Log Out"

        api.insertAccountLog(logData).enqueue(new Callback<Void>() {
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



}

