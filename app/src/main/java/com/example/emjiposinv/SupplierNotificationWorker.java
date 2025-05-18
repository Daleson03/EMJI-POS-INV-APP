package com.example.emjiposinv;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupplierNotificationWorker extends Worker {
    private static final String CHANNEL_ID = "supplier_channel";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";
    private static final String CONTENT_TYPE = "application/json";
    private SupabaseAuthApi supabaseAuthApi;

    public SupplierNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("SupplierNotificationWorker", "doWork() started.");

        List<SupplierNotif> suppliernotifs = fetchSuppliersFromSupabase();
        Log.d("SupplierNotificationWorker", "Number of suppliers fetched: " + suppliernotifs.size());

        if (suppliernotifs.isEmpty()) {
            Log.d("SupplierNotificationWorker", "No suppliers to process.");
            return Result.success();
        }

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        long twoHoursMillis = 2 * 60 * 60 * 1000;

        // Group suppliers by cutoff time if within 2 hours
        Map<String, List<SupplierNotif>> groupedByCutoff = new HashMap<>();

        for (SupplierNotif supplier : suppliernotifs) {
            try {
                Date cutoffDate = format.parse(supplier.getCutoffTime());
                if (cutoffDate == null) continue;

                Calendar cutoffCal = Calendar.getInstance();
                cutoffCal.setTime(cutoffDate);
                cutoffCal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

                long diffMillis = cutoffCal.getTimeInMillis() - now.getTimeInMillis();

                if (diffMillis <= twoHoursMillis && diffMillis > 0) {
                    String cutoffTimeKey = supplier.getCutoffTime();
                    groupedByCutoff.computeIfAbsent(cutoffTimeKey, k -> new ArrayList<>()).add(supplier);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Send notifications per cutoff group
        for (Map.Entry<String, List<SupplierNotif>> entry : groupedByCutoff.entrySet()) {
            String cutoffTime = entry.getKey();
            List<SupplierNotif> group = entry.getValue();

            if (group.size() == 1) {
                sendNotification(group.get(0));
            } else {
                sendGroupedNotification(group, cutoffTime);
            }
        }

        Log.d("SupplierNotificationWorker", "Worker finished.");
        return Result.success();
    }

    private List<SupplierNotif> fetchSuppliersFromSupabase() {
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);

        Call<List<SupplierNotif>> call = supabaseAuthApi.getSuppliers(
                API_KEY,
                AUTH_TOKEN,
                CONTENT_TYPE,
                "SupplierName,CutoffTime"
        );

        try {
            Response<List<SupplierNotif>> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                Log.e("WorkerError", "Error: " + response.code());
            }
        } catch (IOException e) {
            Log.e("WorkerError", "Exception: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    private void sendNotification(SupplierNotif suppliernotif) {
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("SupplierNotificationWorker", "Notification permission not granted.");
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Supplier Cutoff Reminder")
                .setContentText("Cutoff time for " + suppliernotif.getSupplierName() + " is at " + suppliernotif.getCutoffTime())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(new Random().nextInt(10000), builder.build());
    }

    private void sendGroupedNotification(List<SupplierNotif> suppliers, String cutoffTime) {
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("SupplierNotificationWorker", "Notification permission not granted.");
                return;
            }
        }

        StringBuilder contentText = new StringBuilder("Cutoff at " + cutoffTime + " for: ");
        for (int i = 0; i < suppliers.size(); i++) {
            contentText.append(suppliers.get(i).getSupplierName());
            if (i < suppliers.size() - 1) contentText.append(", ");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Supplier Cutoff Reminder")
                .setContentText(contentText.toString())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText.toString()))
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(new Random().nextInt(10000), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Supplier Cutoff Notifications";
            String description = "Notifies 2 hours before supplier cutoff time";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
