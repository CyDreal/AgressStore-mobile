package com.example.agress;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.agress.databinding.ActivityMainBinding;
import com.example.agress.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Get NavHostFragment and NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();

        // Setup bottom navigation with custom listener
        binding.navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_profile && sessionManager.isGuest()) {
                showLoginRequiredDialog();
                return false;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        // Setup bottom navigation
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_product, R.id.navigation_dashboard,
                R.id.navigation_cart, R.id.navigation_profile)
                .build();

        // Setup NavigationUI
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void showLoginRequiredDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Login Required")
                .setMessage("You need to login to access profile features")
                .setPositiveButton("Login", (dialog, which) -> {
                    Intent intent = new Intent(this, UserIdentifyActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Navigate to product fragment
                    navController.navigate(R.id.navigation_product);
                })
                .setCancelable(false)
                .show();
    }

    public void hideBottomNavigation() {
        if (binding.navView != null) {
            binding.navView.setVisibility(View.GONE);
        }
    }

    public void showBottomNavigation() {
        if (binding.navView != null) {
            binding.navView.setVisibility(View.VISIBLE);
        }
    }
}