package com.example.emjiposinv;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class StockCheckWorker extends Worker {

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzkwOTk3MTUsImV4cCI6MjA1NDY3NTcxNX0.xSWcq2JYTeI1W4orV68VhZZ4c7EvVMPDhoIGzqieb00";
    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhbGt2dWF5Zm5xaHFlbHFidGRrIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczOTA5OTcxNSwiZXhwIjoyMDU0Njc1NzE1fQ.-5i8l782wwDTcrauQMGzVIwlDBI7KkSzbpoegWCQAJA";
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHANNEL_ID = "stock_alerts";

    private static final String GROUP_KEY_STOCK_ALERTS = "com.example.emjiposinv.STOCK_ALERTS";


    private SupabaseAuthApi supabaseAuthApi;

    public StockCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        supabaseAuthApi = RetrofitClient.getClient().create(SupabaseAuthApi.class);
        supabaseAuthApi.getAllProductsforStockAlert(API_KEY, AUTH_TOKEN, CONTENT_TYPE).enqueue(new Callback<List<ProductStockAlert>>() {
            @Override
            public void onResponse(Call<List<ProductStockAlert>> call, Response<List<ProductStockAlert>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ProductStockAlert productstockalert : response.body()) {
                        int qty = productstockalert.getQuantity();
                        if (qty <= 0) {
                            sendNotification(productstockalert.getProductId(), productstockalert.getBatchNumber(), productstockalert.getProductName(), qty, "Out of Stock");
                        } else if (qty < 10) {
                            sendNotification(productstockalert.getProductId(), productstockalert.getBatchNumber(), productstockalert.getProductName(), qty, "Needs Restock");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ProductStockAlert>> call, Throwable t) {
                t.printStackTrace();
            }
        });

        return Result.success();
    }

    private void sendNotification(String productId, String batchNumber, String productName, int quantity, String stockStatus) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Stock Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alerts for low/out-of-stock products");
            manager.createNotificationChannel(channel);
        }

        String title = stockStatus + ": " + productName;
        String content = "Product ID: " + productId + "\nBatch #: " + batchNumber + "\nQty: " + quantity;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning) // Replace with your icon
                .setContentTitle(title)
                .setContentText("Tap to view")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setGroup(GROUP_KEY_STOCK_ALERTS)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Send individual notification
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        // Send summary notification
        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Stock Alerts")
                .setContentText("You have stock alerts")
                .setSmallIcon(R.drawable.ic_warning)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(title))
                .setGroup(GROUP_KEY_STOCK_ALERTS)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify(0, summaryBuilder.build()); // Use ID 0 for the summary
    }

}

