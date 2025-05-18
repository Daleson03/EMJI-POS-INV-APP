    package com.example.emjiposinv;

    import android.Manifest;
    import android.content.pm.PackageManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.text.InputType;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.content.Intent;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.ActionBar;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.app.NotificationCompat;
    import androidx.core.app.NotificationManagerCompat;
    import androidx.core.content.ContextCompat;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.work.Constraints;
    import androidx.work.ExistingPeriodicWorkPolicy;
    import androidx.work.NetworkType;
    import androidx.work.OneTimeWorkRequest;
    import androidx.work.PeriodicWorkRequest;
    import androidx.work.WorkManager;

    import java.io.IOException;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Locale;
    import java.util.Map;
    import java.util.Random;
    import java.util.concurrent.TimeUnit;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class MainActivity extends AppCompatActivity {
        private AuthManager authManager;
        private boolean isPasswordVisible = false;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide(); // Only hide if the ActionBar exists
            }
            setContentView(R.layout.activity_main);

            // Handle window insets for Edge-to-Edge layout
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // Initialize authentication manager
            authManager = new AuthManager(this);


            // Find UI elements
            EditText email = findViewById(R.id.txtusername);
            EditText passwordEditText = findViewById(R.id.txtpassword);
            Button loginButton = findViewById(R.id.btnlogin);
            Button signupButton = findViewById(R.id.signupButton);
            Button forgotPasswordButton = findViewById(R.id.btnlogin2); // Forgot Password button
            ImageButton togglePasswordButton = findViewById(R.id.btnTogglePassword);

            // Login button click listener
            loginButton.setOnClickListener(v -> authManager.signInUser(email.getText().toString(), passwordEditText.getText().toString()));

            // Signup button click listener
            signupButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            });

            // Forgot Password button click listener
            forgotPasswordButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ResetPasswordEmailActivity.class);
                startActivity(intent);
            });

            // Toggle password visibility button
            togglePasswordButton.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    // Hide password
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordButton.setImageResource(R.drawable.iconhidepass); // Open eye icon
                } else {
                    // Show password
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordButton.setImageResource(R.drawable.iconshowpass); // Closed eye icon
                }
                isPasswordVisible = !isPasswordVisible;

                // Move cursor to end after changing input type
                passwordEditText.setSelection(passwordEditText.getText().length());
            });

            // Schedule the daily notification
            scheduleDailyNotification();
            //triggerTestNotificationWorker();
            //triggerStockAlertNotification();
        }

            private void scheduleDailyNotification() {
                // Check if the app has permission to post notifications
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Request the permission
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
                    } else {
                        // Permissions are granted, proceed with scheduling
                        scheduleNotificationWorker();
                        StockAlertNotificationWorker();
                    }
                } else {
                    // For devices below Android 13, no need to check permission
                    scheduleNotificationWorker();
                    StockAlertNotificationWorker();
                }
            }

        private void scheduleNotificationWorker() {
            // Create a periodic work request that runs daily
            PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                    SupplierNotificationWorker.class,
                    15, TimeUnit.MINUTES // Runs every 1 hours
            ).build();

            // Enqueue the work request
            WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                    "SupplierCutoffNotification",
                    ExistingPeriodicWorkPolicy.KEEP,
                    dailyWorkRequest
            );
        }

        private void StockAlertNotificationWorker() {
            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                    StockCheckWorker.class,
                    30, TimeUnit.MINUTES
            ).build();

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "StockCheckJob",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
            );
        }

        private void triggerStockAlertNotification() {
            OneTimeWorkRequest testRequest = new OneTimeWorkRequest.Builder(StockCheckWorker.class)
                    .build();

            WorkManager.getInstance(getApplicationContext()).enqueue(testRequest);
        }

        private void triggerTestNotificationWorker() {
            OneTimeWorkRequest testRequest = new OneTimeWorkRequest.Builder(SupplierNotificationWorker.class)
                    .build();

            WorkManager.getInstance(getApplicationContext()).enqueue(testRequest);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == 100) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted → schedule the notification worker
                    scheduleNotificationWorker();
                    StockAlertNotificationWorker();
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }


    }
