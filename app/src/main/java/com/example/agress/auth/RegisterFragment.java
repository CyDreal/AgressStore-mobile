package com.example.agress.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.agress.R;
import com.example.agress.UserIdentifyActivity;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.RegisterRequest;
import com.example.agress.databinding.FragmentRegisterBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        binding.registerButton.setOnClickListener(v -> attempRegister());
        return binding.getRoot();
    }

    private void attempRegister() {
        String username = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Tolong isi semua kolom", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        ApiClient.getClient().register(registerRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse apiResponse = response.body();
                    if (apiResponse.getStatus() == 1) {
                        Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        // Navigate to login fragment or main activity
                        if (getActivity() instanceof UserIdentifyActivity) {
                            ((UserIdentifyActivity) getActivity()).switchToLogin();
                        }
                    } else {
                        Toast.makeText(requireContext(),apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                Toast.makeText(requireContext(), "Network error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}