package com.example.emjiposinv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Re-schedule the periodic work
            PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                    SupplierNotificationWorker.class,
                    1, TimeUnit.DAYS
            ).build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "SupplierCutoffNotification",
                    ExistingPeriodicWorkPolicy.KEEP,
                    dailyWorkRequest
            );
        }
    }
}