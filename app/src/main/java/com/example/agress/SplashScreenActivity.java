package com.example.agress;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.agress.utils.SessionManager;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3s

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        // Set fullscreen flags
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Initialize session manager
        SessionManager sessionManager = new SessionManager(this);

        // Setup animation
        LottieAnimationView animationView = findViewById(R.id.lottie_splash);
        animationView.playAnimation();

        new Handler().postDelayed(() -> {
            // Identify if user is logged in or not
            boolean isLoggedIn = sessionManager.isLoggedIn();
            String userId = sessionManager.getUserId();
            String userName = sessionManager.getUsername();

            if (isLoggedIn && userId != null && !userId.isEmpty()) {
                // User is logged in, proceed to MainActivity
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // User is not logged in, proceed to UserIdentifyActivity
                Intent intent = new Intent(SplashScreenActivity.this, UserIdentifyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
        }, SPLASH_DURATION);
    }
}