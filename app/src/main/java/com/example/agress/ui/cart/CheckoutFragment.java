package com.example.agress.ui.cart;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.agress.R;
import com.example.agress.adapter.AddressSelectionAdapter;
import com.example.agress.adapter.CheckoutItemAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.AddressResponse;
import com.example.agress.databinding.FragmentCheckoutBinding;
import com.example.agress.model.Address;
import com.example.agress.model.CartItem;
import com.example.agress.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {
    private FragmentCheckoutBinding binding;
    private SessionManager sessionManager;
    private CheckoutItemAdapter adapter;
    private static final int SHIPPING_COST = 10000; // Example shipping cost
    private Address selectedAddress;

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

        // Setup address selection
        binding.cardAddress.setOnClickListener(v -> showAddressSelectionDialog());

        // Setup back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }

    private void showAddressSelectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_select_address, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerAddresses);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        AddressSelectionAdapter adapter = new AddressSelectionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setListener(address -> {
            selectedAddress = address;
            updateAddressDisplay();
            dialog.dismiss();
        });

        // Load addresses
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getClient().getAddresses(sessionManager.getUserId())
                .enqueue(new Callback<AddressResponse>() {
                    @Override
                    public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setAddresses(response.body().getAddresses());
                        } else {
                            Toast.makeText(requireContext(),
                                    "Failed to load addresses",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AddressResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }

    private void updateAddressDisplay() {
        if (selectedAddress != null) {
            String fullAddress = String.format("%s\n%s\n%s, %s, %s\n%s",
                    selectedAddress.getLabel(),
                    selectedAddress.getRecipientName(),
                    selectedAddress.getFullAddress(),
                    selectedAddress.getCityName(),
                    selectedAddress.getProvinceName(),
                    selectedAddress.getPostalCode());
            binding.textAddress.setText(fullAddress);
        }
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