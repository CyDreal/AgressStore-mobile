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
import com.example.agress.api.response.CartListResponse;
import com.example.agress.api.response.UserResponse;
import com.example.agress.api.response.request.LoginRequest;
import com.example.agress.databinding.FragmentLoginBinding;
import com.example.agress.model.Cart;
import com.example.agress.model.CartItem;
import com.example.agress.model.User;
import com.example.agress.utils.SessionManager;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        sessionManager = new SessionManager(requireContext());

        binding.loginButton.setOnClickListener(v -> attemptLogin());
        return binding.getRoot();
    }

    private void attemptLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(email, password);
        ApiClient.getClient().login(loginRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null) {
                        sessionManager.saveUser(user);
                        loadUserCart(user.getUserId());
                    } else {
                        Toast.makeText(requireContext(), "Invalid user data received", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(requireContext(), errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(requireContext(),
                    "Network error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserCart(String userId) {
        ApiClient.getClient().getUserCarts(userId)
                .enqueue(new Callback<CartListResponse>() {
                    @Override
                    public void onResponse(Call<CartListResponse> call,
                                        Response<CartListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Cart> carts = response.body().getCarts();
                            updateSessionManagerCart(carts);
                        }
                        navigateToMain();
                    }

                    @Override
                    public void onFailure(Call<CartListResponse> call, Throwable t) {
                        navigateToMain();
                    }
                });
    }

    private void updateSessionManagerCart(List<Cart> carts) {
        sessionManager.clearCart();
        for (Cart cart : carts) {
            CartItem item = cart.toCartItem();
            if (item != null) {
                sessionManager.addToCart(item);
            }
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}