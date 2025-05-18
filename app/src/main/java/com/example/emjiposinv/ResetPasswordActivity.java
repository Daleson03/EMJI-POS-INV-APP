package com.example.emjiposinv;

import static com.example.emjiposinv.ResetPasswordEmailActivity.EmailforTB;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordInput;
    private Button resetPasswordButton;
    private String accessToken;
    private SupabaseAuthApi supabaseAuthApi;

    private static final String SUPABASE_URL = "https://balkvuayfnqhqelqbtdk.supabase.co";

    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt" +
            "2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7E" +
            "vVMPDhoIGzqieb00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_reset_password);

        newPasswordInput = findViewById(R.id.newPassword);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        // Extract access token from deep link
        handleDeepLink(getIntent());

        // Reset password button click listener
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = newPasswordInput.getText().toString().trim();
                Updateaccount();
                if (!TextUtils.isEmpty(newPassword)) {
                    resetPassword(newPassword);
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Enter a valid password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void Updateaccount() {
        Map<String, Object> updateaccount = new HashMap<>();
        updateaccount.put("password", newPasswordInput.getText().toString());

        String filter = "eq." + EmailforTB; //  Correct Supabase filter

        supabaseAuthApi.updateaccount(filter, updateaccount).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Update Password in AccountTB","Reset Password Successfully!");
                } else {
                    try {
                        //  Print error response for debugging
                        Toast.makeText(ResetPasswordActivity.this, "Update Failed: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ResetPasswordActivity.this, "Update Failed: Unable to read error", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String fragment = data.getFragment();
            if (fragment != null) {
                for (String param : fragment.split("&")) {
                    if (param.startsWith("access_token=")) {
                        accessToken = param.split("=")[1];
                        break;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(accessToken)) {
            Toast.makeText(this, "Invalid or expired reset link", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void resetPassword(String newPassword) {
        if (accessToken == null) {
            Toast.makeText(this, "Invalid or expired reset token", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        // Prepare JSON request body
        String json = "{\"password\":\"" + newPassword + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        // Create HTTP request
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/user")
                .put(body)  // 🔥 Change PATCH to PUT
                .addHeader("Authorization", "Bearer " + accessToken)  // Required token
                .addHeader("apikey", SUPABASE_ANON_KEY)  //  Required API key
                .addHeader("Content-Type", "application/json")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                String responseMessage = response.message();
                runOnUiThread(() -> {
                    int statusCode = response.code();
                    if (response.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        String messageToShow = !responseBody.isEmpty() ? responseBody : responseMessage;
                        Toast.makeText(ResetPasswordActivity.this, messageToShow, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ResetPasswordActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
