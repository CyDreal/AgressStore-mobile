package com.example.agress.ui.cart;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agress.R;
import com.example.agress.adapter.CartAdapter;
import com.example.agress.databinding.FragmentCartBinding;
import com.example.agress.model.CartItem;
import com.example.agress.utils.SessionManager;

import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.CartItemListener {

    private FragmentCartBinding binding;
    private SessionManager sessionManager;
    private CartAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        sessionManager = new SessionManager(requireContext());

        if (sessionManager.isGuest()) {
            showEmptyCart();
        } else {
            setupRecyclerView();
            loadCartItems();
        }

        // Add click listener for checkout button
        binding.btnCheckout.setOnClickListener(v -> {
            if (!sessionManager.isGuest() && !sessionManager.getCartItems().isEmpty()) {
                // Navigate to checkout
                Navigation.findNavController(v).navigate(R.id.action_cart_to_checkout);
            }
        });

        return binding.getRoot();
    }


    private void showEmptyCart() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.textTotalPrice.setVisibility(View.GONE);
        binding.btnCheckout.setVisibility(View.GONE);
        binding.emptyCartText.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this, requireContext()); // Fixed constructor
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadCartItems() {
        List<CartItem> cartItems = sessionManager.getCartItems();
        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.textTotalPrice.setVisibility(View.VISIBLE);
            binding.btnCheckout.setVisibility(View.VISIBLE);
            binding.emptyCartText.setVisibility(View.GONE);
            adapter.setItems(cartItems);
            updateTotalPrice();
        }
    }

    private void updateTotalPrice() {
        List<CartItem> items = sessionManager.getCartItems();
        int total = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
        binding.textTotalPrice.setText(String.format("Total: Rp %,d", total));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        // Optional: Refresh cart items to ensure consistency
        loadCartItems();

        // Refresh total price
        updateTotalPrice();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        loadCartItems();
        updateTotalPrice();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!sessionManager.isGuest()) {
            loadCartItems();
        }
    }

    @Override
    public void showLoading() {
        if (binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (binding != null) {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}