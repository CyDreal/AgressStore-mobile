package com.example.agress.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.agress.MainActivity;
import com.example.agress.R;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.LoginRequest;
import com.example.agress.databinding.FragmentLoginBinding;
import com.example.agress.model.User;
import com.example.agress.utils.SessionManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        binding.loginButton.setOnClickListener(v -> attempLogin());

        return binding.getRoot();
    }

    private void attempLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Tolong isi semua kolom", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(email, password);
        ApiClient.getClient().login(loginRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse apiResponse = response.body();
                    if (apiResponse.getStatus() == 1 && apiResponse.getUser() != null) {
                        User user = apiResponse.getUser();

                        // Debug log
                        System.out.println("Debug - login Response:");
                        System.out.println("User ID: " + user.getUserId());
                        System.out.println("Username: " + user.getUsername());

                        // Membuat Session
                        SessionManager sessionManager = new SessionManager(requireContext());
                        sessionManager.createLoginSession(
                                user.getUserId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getAddress(),
                                user.getCity(),
                                user.getProvince(),
                                user.getPhone(),
                                user.getPostalCode()
                        );

                        // Verify session
                        if (sessionManager.getUserId() == null || sessionManager.getUserId().isEmpty()) {
                            Toast.makeText(requireContext(), "Gagal untuk menyimpan session", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(requireContext(),apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(requireActivity(), MainActivity.class));
//                        requireActivity().finish();
                        // Simple navigation
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        // Handle unsuccessful login with message from API
                        Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        // Handle error response
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() :
                                "Unknown error occurred";
                        Toast.makeText(requireContext(), errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                Toast.makeText(requireContext(), "Network erro: " + throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}