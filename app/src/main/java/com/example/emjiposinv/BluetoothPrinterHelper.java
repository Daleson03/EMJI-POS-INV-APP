package com.example.emjiposinv;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.IOException;
public class BluetoothPrinterHelper {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    public BluetoothPrinterHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public List<BluetoothDevice> getAvailableDevices(Context context) {
        List<BluetoothDevice> deviceList = new ArrayList<>();

        // Check if permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return deviceList; // Return empty list if permission is not granted
            }
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            deviceList.addAll(pairedDevices);
        }

        return deviceList;
    }

    public boolean connectToPrinter(Context context, BluetoothDevice device) {
        // Check if permission is granted before attempting to connect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void printImage(Bitmap bitmap) {
        if (outputStream == null) {
            return;
        }

        Bitmap resizedBitmap = convertToMonochrome(bitmap);

        try {
            // Send ESC/POS command to center the image
            outputStream.write(new byte[]{0x1B, 0x61, 0x01}); // ESC a 1 (Center alignment)

            byte[] imageBytes = decodeBitmap(resizedBitmap);
            outputStream.write(imageBytes);

            outputStream.write("\n\n".getBytes()); // Newline after printing
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Convert the image to black and white
    private Bitmap convertToMonochrome(Bitmap bitmap) {
        int width = 384; // Match the printer width
        int height = (bitmap.getHeight() * width) / bitmap.getWidth();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        Bitmap monochromeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(monochromeBitmap);
        Paint paint = new Paint();

        // Set the background to white
        canvas.drawColor(Color.WHITE);

        // Draw the original image on top
        canvas.drawBitmap(resizedBitmap, 0, 0, paint);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                int grayscale = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;

                // Convert to black & white using thresholding
                if (grayscale < 128) {
                    monochromeBitmap.setPixel(x, y, Color.BLACK); // Keep black pixels
                } else {
                    monochromeBitmap.setPixel(x, y, Color.WHITE); // Ensure white background
                }
            }
        }
        return monochromeBitmap;
    }

    private byte[] decodeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bytesPerLine = width / 8;
        int bitmapSize = bytesPerLine * height;

        byte[] imageBytes = new byte[bitmapSize + 8];

        // ESC * Command for raster bit image mode
        imageBytes[0] = 0x1D;
        imageBytes[1] = 0x76;
        imageBytes[2] = 0x30;
        imageBytes[3] = 0x00;
        imageBytes[4] = (byte) (width / 8);
        imageBytes[5] = 0x00;
        imageBytes[6] = (byte) (height % 256);
        imageBytes[7] = (byte) (height / 256);

        int index = 8;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 8) {
                byte pixelByte = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int pixel = bitmap.getPixel(x + bit, y);
                    if (pixel == Color.BLACK) { // Only print black pixels
                        pixelByte |= (1 << (7 - bit));
                    }
                }
                imageBytes[index++] = pixelByte;
            }
        }
        return imageBytes;
    }


    public void printText(String text, boolean isBold, boolean isUnderlined, int alignment) {
        if (outputStream == null) {
            return;
        }

        try {
            // ESC/POS Command: Reset Printer
            outputStream.write(new byte[]{0x1B, 0x40});

            // ESC/POS Command: Text Alignment
            if (alignment == 0) {
                outputStream.write(new byte[]{0x1B, 0x61, 0x00}); // Left
            } else if (alignment == 1) {
                outputStream.write(new byte[]{0x1B, 0x61, 0x01}); // Center
            } else if (alignment == 2) {
                outputStream.write(new byte[]{0x1B, 0x61, 0x02}); // Right
            }

            // ESC/POS Command: Bold Text
            if (isBold) {
                outputStream.write(new byte[]{0x1B, 0x45, 0x01});
            } else {
                outputStream.write(new byte[]{0x1B, 0x45, 0x00});
            }

            // ESC/POS Command: Underline Text
            if (isUnderlined) {
                outputStream.write(new byte[]{0x1B, 0x2D, 0x01});
            } else {
                outputStream.write(new byte[]{0x1B, 0x2D, 0x00});
            }

            // Send text
            outputStream.write(text.getBytes("UTF-8"));

            // New Line
            outputStream.write("\n".getBytes());

            // Reset styles after printing
            outputStream.write(new byte[]{0x1B, 0x45, 0x00}); // Disable Bold
            outputStream.write(new byte[]{0x1B, 0x2D, 0x00}); // Disable Underline

            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
