package com.example.agress.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.agress.MainActivity;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.UpdateProfileRequest;
import com.example.agress.databinding.FragmentEditProfileBinding;
import com.example.agress.model.User;
import com.example.agress.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        sessionManager = new SessionManager(requireContext());

        // Hide bottom navigation
        hideBottomNavigation();

        // Setup toolbar
        setupToolbar();

        // Load current user data
        loadUserData();

        // Setup save button
        setupSaveButton();

        return binding.getRoot();
    }

    private void hideBottomNavigation() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideBottomNavigation();
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void loadUserData() {
        User user = sessionManager.getUser();
        if (user != null) {
            binding.editUsername.setText(user.getUsername());
            binding.editAddress.setText(user.getAddress());
            binding.editCity.setText(user.getCity());
            binding.editProvince.setText(user.getProvince());
            binding.editPhone.setText(user.getPhone());
            binding.editPostalCode.setText(user.getPostalCode());
        }
    }

    private void setupSaveButton() {
        binding.buttonSave.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfile();
            }
        });
    }

    private boolean validateInputs() {
        if (binding.editUsername.getText().toString().trim().isEmpty()) {
            binding.editUsername.setError("Username is required");
            return false;
        }
        return true;
    }

    private void updateProfile() {
        showLoading(true);
        User currentUser = sessionManager.getUser();
        if (currentUser == null) {
            showError("User data not found");
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(
                binding.editUsername.getText().toString(),
                binding.editAddress.getText().toString(),
                binding.editCity.getText().toString(),
                binding.editProvince.getText().toString(),
                binding.editPhone.getText().toString(),
                binding.editPostalCode.getText().toString()
        );

        ApiClient.getClient()
                .updateProfile(currentUser.getUserId(), request)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserResponse> call,
                                           @NonNull Response<UserResponse> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            sessionManager.saveUser(response.body().getUser());
                            showMessage("Profile updated successfully");
                            Navigation.findNavController(requireView()).navigateUp();
                        } else {
                            showError("Failed to update profile");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        showError("Network error: " + t.getMessage());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        // Show bottom navigation when leaving
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showBottomNavigation();
        }
        super.onDestroyView();
        binding = null;
    }

    private void showLoading(boolean show) {
        binding.buttonSave.setEnabled(!show);
        // Add progress indicator if needed
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}