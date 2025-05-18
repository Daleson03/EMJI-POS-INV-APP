package com.example.emjiposinv;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class ResetPasswordEmailActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button btnSendReset;

    public static String EmailforTB;

    private static final String SUPABASE_URL = "https://balkvuayfnqhqelqbtdk.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhY" +
            "mFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI" +
            "6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqi" +
            "eb00"; // Use anon key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_email);

        emailInput = findViewById(R.id.editTextTextEmailAddress);
        btnSendReset = findViewById(R.id.btnemail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnSendReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(ResetPasswordEmailActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                } else {
                    checkIfEmailExists(email);
                }
            }
        });
    }

    private void checkIfEmailExists(String email) {
        OkHttpClient client = new OkHttpClient();

        // JSON request body for calling the function
        String json = "{\"email_input\":\"" + email + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        // Supabase RPC endpoint
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/rpc/check_email_exists")
                .post(body)
                .addHeader("apikey", SUPABASE_ANON_KEY) // Use anon key
                .addHeader("Content-Type", "application/json")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    if (response.isSuccessful() && responseBody.contains("true")) {
                        sendResetPasswordLink(email);// Email found, send reset link

                        EmailforTB = email;

                    } else {
                        Toast.makeText(ResetPasswordEmailActivity.this, "Email not found!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(ResetPasswordEmailActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendResetPasswordLink(String email) {
        OkHttpClient client = new OkHttpClient();

        String json = "{\"email\":\"" + email + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/auth/v1/recover")
                .newBuilder()
                .addQueryParameter("redirect_to", "myapp://reset-password")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ResetPasswordEmailActivity.this, "Reset link sent!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ResetPasswordEmailActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(ResetPasswordEmailActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

}
