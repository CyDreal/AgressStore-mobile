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
            if (sessionManager.isLoggedIn()) {
                // User is logged in, go to MainActivity
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // User is not logged in, set as guest and go to MainActivity
                if (!sessionManager.isGuest()) {  // Only set guest session if not already a guest
                    sessionManager.setGuestSession();
                }
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }, SPLASH_DURATION);
    }
}