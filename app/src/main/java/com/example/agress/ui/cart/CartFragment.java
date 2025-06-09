package com.example.agress.ui.cart;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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

        return binding.getRoot();
    }

    private void showEmptyCart() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.textTotalPrice.setVisibility(View.GONE);

        // Add TextView for empty cart message if not exists in layout
        TextView emptyText = new TextView(requireContext());
        emptyText.setText("Your cart is empty");
        emptyText.setTextSize(16);
        emptyText.setGravity(Gravity.CENTER);

        // Add TextView to layout
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        ((ConstraintLayout) binding.getRoot()).addView(emptyText, params);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
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
        // Update SessionManager
        sessionManager.updateCartItemQuantity(item.getProductId(), newQuantity);

        // Refresh total price
        updateTotalPrice();

        // Optional: Refresh cart items to ensure consistency
        loadCartItems();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        sessionManager.removeFromCart(item.getProductId());
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
}