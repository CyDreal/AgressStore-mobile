package com.example.agress.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.agress.R;
import com.example.agress.UserIdentifyActivity;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.databinding.FragmentSettingBinding;
import com.example.agress.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        // Get reference to bottom navigation
        bottomNav = requireActivity().findViewById(R.id.nav_view);

        // Hide bottom navigation when entering settings
        hideBottomNavigation();

        setupBackButton();
        setupPasswordReset();
    }

    private void setupPasswordReset() {
        binding.btnChangePassword.setOnClickListener(v -> {
            String currentPassword = binding.etCurrentPassword.getText().toString().trim();
            String newPassword = binding.etNewPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            // Validate inputs
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                binding.tilConfirmPassword.setError("Passwords do not match");
                return;
            }

            if (newPassword.length() < 6) {
                binding.tilNewPassword.setError("Password must be at least 6 characters");
                return;
            }

            // Show loading dialog
            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Changing password...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Call API
            ApiClient.getClient().resetPassword(
                    sessionManager.getUserId(),
                    currentPassword,
                    newPassword,
                    confirmPassword
            ).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getStatus() == 1) {
                            showSuccessDialog();
                        } else {
                            Toast.makeText(requireContext(),
                                    response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Failed to change password",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(),
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showSuccessDialog() {
        // Logout user
        sessionManager.logout();

        // Show brief success message
        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();

        // Navigate to UserIdentifyActivity immediately
        Intent intent = new Intent(requireContext(), UserIdentifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            // Navigate back to profile
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void hideBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void showBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show bottom navigation when leaving settings
        showBottomNavigation();
        binding = null;
    }
}