package com.example.agress.ui.profile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.agress.R;
import com.example.agress.adapter.OrderItemAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.OrderDetailResponse;
import com.example.agress.api.response.OrderResponse;
import com.example.agress.databinding.FragmentOrderDetailsBinding;
import com.example.agress.model.Order;
import com.example.agress.model.OrderItem;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsFragment extends Fragment {
    private FragmentOrderDetailsBinding binding;
    private ApiService apiService;
    private OrderItemAdapter itemAdapter;
    private int orderId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get order ID from arguments
        Bundle args = getArguments();
        if (args == null) {
            showError("Invalid order ID");
            return;
        }

        orderId = args.getInt("order_id", -1);
        if (orderId == -1) {
            showError("Invalid order ID");
            return;
        }
        
        // Initialize API service
        apiService = ApiClient.getClient();

        // Initialize RecyclerView
        itemAdapter = new OrderItemAdapter();
        binding.recyclerOrderItems.setAdapter(itemAdapter);
        binding.recyclerOrderItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        // Load order details
        loadOrderDetails();
    }

    private void loadOrderDetails() {
        apiService.getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call,
                                   @NonNull Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body().getOrder();
                    if (order != null) {
                        displayOrderDetails(order);
                    } else {
                        showError("Order not found");
                    }
                } else {
                    showError("Failed to load order details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    private void displayOrderDetails(Order order) {
        if (!isAdded()) return;

        // Set order info
        binding.textOrderId.setText(getString(R.string.order_number, order.getId()));
        binding.textOrderDate.setText(formatDate(order.getCreatedAt()));
        binding.textStatus.setText(order.getPaymentStatus());
        binding.textStatus.setBackgroundTintList(ColorStateList.valueOf(
                getStatusColor(order.getPaymentStatus())));
        binding.textPaymentMethod.setText(getString(R.string.payment_method,
                order.getPaymentMethod().toUpperCase()));

        // Set transaction ID
        if (order.getPaymentToken() != null && !order.getPaymentToken().isEmpty()) {
            binding.textTransactionId.setVisibility(View.VISIBLE);
            binding.textTransactionId.setText(getString(R.string.transaction_id, order.getPaymentToken()));
        } else {
            binding.textTransactionId.setVisibility(View.GONE);
        }

        // Set up WebView for payment URL if available
        if (order.getPaymentUrl() != null && !order.getPaymentUrl().isEmpty()) {
            binding.webViewDetails.setVisibility(View.VISIBLE);
            setupWebView(order.getPaymentUrl());
        } else {
            binding.webViewDetails.setVisibility(View.GONE);
        }

        // Set shipping address
        String fullAddress = String.format("%s\n%s, %s\n%s",
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingProvince(),
                order.getShippingPostalCode());
        binding.textShippingAddress.setText(fullAddress);

        // Set order items
        itemAdapter.setItems(order.getItems());

        // Set order summary
        binding.textSubtotal.setText(formatPrice(calculateSubtotal(order.getItems())));
        binding.textShippingCost.setText(formatPrice(order.getShippingCost()));
        binding.textTotal.setText(formatPrice(order.getTotalPrice()));
    }

    private void setupWebView(String paymentUrl) {
        WebView webView = binding.webViewDetails;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Handle URL loading within the WebView
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Hide loading indicator if you have one
            }
        });

        // Load the payment URL
        webView.loadUrl(paymentUrl);
    }

    private String calculateSubtotal(List<OrderItem> items) {
        if (items == null) return "0";

        double total = 0;
        for (OrderItem item : items) {
            if (item != null && item.getSubtotal() != null) {
                try {
                    total += Double.parseDouble(item.getSubtotal());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return String.valueOf(total);
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id"));
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String formatPrice(String price) {
        try {
            double amount = Double.parseDouble(price);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return formatter.format(amount);
        } catch (NumberFormatException e) {
            return price;
        }
    }

    private int getStatusColor(String status) {
        Context context = requireContext();
        switch (status.toLowerCase()) {
            case "paid":
                return context.getColor(R.color.success);
            case "unpaid":
                return context.getColor(R.color.error);
            default:
                return context.getColor(R.color.primary);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}