package com.example.emjiposinv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinSettingsActivity extends AppCompatActivity {

    private EditText editCurrentPin, editNewPin, editConfirmPin;
    private Button btnSavePin;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_settings);

        editCurrentPin = findViewById(R.id.editCurrentPin);
        editNewPin = findViewById(R.id.editNewPin);
        editConfirmPin = findViewById(R.id.editConfirmPin);
        btnSavePin = findViewById(R.id.btnSavePin);
        Button btnResetPin = findViewById(R.id.btnResetPin);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedPin = prefs.getString("secure_pin", "1234"); // default if none saved

        // Set max length and input type for PIN fields
        InputFilter[] pinLengthFilter = new InputFilter[]{new InputFilter.LengthFilter(4)};
        editCurrentPin.setFilters(pinLengthFilter);
        editNewPin.setFilters(pinLengthFilter);
        editConfirmPin.setFilters(pinLengthFilter);

        editCurrentPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editNewPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editConfirmPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        btnResetPin.setOnClickListener(v -> {
            new AlertDialog.Builder(PinSettingsActivity.this)
                    .setTitle("Reset PIN")
                    .setMessage("Are you sure you want to reset the PIN to default (1234)?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("secure_pin", "1234");
                        editor.apply();
                        Toast.makeText(PinSettingsActivity.this, "PIN reset to default (1234)", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });

        btnSavePin.setOnClickListener(v -> {
            String currentPin = editCurrentPin.getText().toString();
            String newPin = editNewPin.getText().toString();
            String confirmPin = editConfirmPin.getText().toString();

            Log.d("pin", savedPin);

            if (!currentPin.equals(savedPin)) {
                Toast.makeText(this, "Incorrect current PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPin.equals(confirmPin)) {
                Toast.makeText(this, "New PINs do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPin.length() != 4) {
                Toast.makeText(this, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("secure_pin", newPin);
            editor.apply();

            Toast.makeText(this, "PIN updated successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close settings screen
        });
    }
}
