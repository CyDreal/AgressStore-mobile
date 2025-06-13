package com.example.agress.ui.cart;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.agress.R;
import com.example.agress.adapter.CheckoutItemAdapter;
import com.example.agress.databinding.FragmentCheckoutBinding;
import com.example.agress.model.CartItem;
import com.example.agress.utils.SessionManager;

import java.util.List;

public class CheckoutFragment extends Fragment {


    private FragmentCheckoutBinding binding;
    private SessionManager sessionManager;
    private CheckoutItemAdapter adapter;
    private static final int SHIPPING_COST = 10000; // Example shipping cost

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize binding first
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize SessionManager
        sessionManager = new SessionManager(requireContext());

        // Hide bottom navigation
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.GONE);

        // Setup views and listeners
        setupViews();

        // Load data
        loadCartItems();
        loadAddress();
        updateSummary();

        return root;
    }

    private void setupViews() {
        // Setup RecyclerView
        adapter = new CheckoutItemAdapter();
        binding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewItems.setAdapter(adapter);

        // Setup back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }

    private void loadCartItems() {
        List<CartItem> cartItems = sessionManager.getCartItems();
        adapter.setItems(cartItems);
    }

    private void loadAddress() {
        String fullAddress = String.format("%s\n%s, %s %s",
                sessionManager.getAddress(),
                sessionManager.getCity(),
                sessionManager.getProvince(),
                sessionManager.getPostalCode());
        binding.textAddress.setText(fullAddress);
    }

    private void updateSummary() {
        List<CartItem> items = sessionManager.getCartItems();

        // Calculate subtotal (total harga items)
        int subtotal = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Add shipping cost
        int totalPrice = subtotal + SHIPPING_COST;

        // Update UI
        binding.textTotalItems.setText(String.format("Total Harga (%d barang)     Rp %,d", items.size(), subtotal));
        binding.textShippingCost.setText(String.format("Ongkos Kirim     Rp %,d", SHIPPING_COST));
        binding.textPriceBottom.setText(String.format("Rp %,d", totalPrice));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show bottom navigation when leaving fragment
        if (getActivity() != null) {
            getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
        }
        // Clear binding to avoid memory leaks
        binding = null;
    }
}