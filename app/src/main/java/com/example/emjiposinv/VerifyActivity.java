package com.example.emjiposinv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class VerifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);

        // Optionally, play the animation again if it was already completed
        lottieAnimationView.playAnimation();

        Toast.makeText(this, "Account verified successfully!", Toast.LENGTH_LONG).show();

        Button btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)); // Or LoginActivity
            finish();
        });
    }
}
