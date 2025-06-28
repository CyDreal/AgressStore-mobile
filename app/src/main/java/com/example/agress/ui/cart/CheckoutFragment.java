package com.example.agress.ui.cart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.agress.R;
import com.example.agress.adapter.AddressSelectionAdapter;
import com.example.agress.adapter.CheckoutItemAdapter;
import com.example.agress.adapter.CourierAdapter;
import com.example.agress.adapter.ServiceAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.AddressResponse;
import com.example.agress.api.response.CourierResponse;
import com.example.agress.api.response.CreateOrderResponse;
import com.example.agress.api.response.MidtransResponse;
import com.example.agress.api.response.OrderDetailResponse;
import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.api.response.ShippingCostResponse;
import com.example.agress.databinding.FragmentCheckoutBinding;
import com.example.agress.model.Address;
import com.example.agress.model.CartItem;
import com.example.agress.model.Courier;
import com.example.agress.model.Order;
import com.example.agress.model.Product;
import com.example.agress.model.ShippingService;
import com.example.agress.utils.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {
    private FragmentCheckoutBinding binding;
    private SessionManager sessionManager;
    private CheckoutItemAdapter adapter;
    private static final int SHIPPING_COST = 10000; // Example shipping cost
    private Address selectedAddress;
    private Courier selectedCourier;
    private ShippingService selectedService;
    private String selectedPaymentMethod = "midtrans";
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

        setupPaymentMethodSelection();

        setupPayButton();

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

        // Setup courier selection
        binding.cardCourier.setOnClickListener(v -> showCourierSelectionDialog());

        // Setup service selection
        binding.cardService.setOnClickListener(v -> showServiceSelectionDialog());

        // Setup back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
    }

    private void showServiceSelectionDialog() {
        if (selectedCourier == null || selectedAddress == null) {
            Toast.makeText(requireContext(), "Pilih kurir dan alamat terlebih dahulu",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_select_service, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerServices);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        ServiceAdapter adapter = new ServiceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Show loading
        progressBar.setVisibility(View.VISIBLE);

        // Make API call
        calculateTotalWeightAsync(weight -> {
            ApiClient.getClient().calculateShipping(
                    "501",
                    selectedAddress.getCityId(),
                    weight,
                    selectedCourier.getCode()
            ).enqueue(new Callback<ShippingCostResponse>() {
                @Override
                public void onResponse(Call<ShippingCostResponse> call,
                                       Response<ShippingCostResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<ShippingService> services =
                                convertToShippingServices(response.body());
                        adapter.setServices(services);
                    }
                }

                @Override
                public void onFailure(Call<ShippingCostResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        adapter.setListener(service -> {
            selectedService = service;
            updateServiceDisplay();
            dialog.dismiss();
        });

        dialog.show();
    }

    private List<ShippingService> convertToShippingServices(ShippingCostResponse response) {
        List<ShippingService> services = new ArrayList<>();
        if (response.getRajaOngkir() != null && response.getRajaOngkir().getResults() != null) {
            for (ShippingCostResponse.Result result : response.getRajaOngkir().getResults()) {
                for (ShippingCostResponse.Cost cost : result.getCosts()) {
                    ShippingService service = new ShippingService();
                    service.setService(cost.getService());
                    service.setDescription(cost.getDescription());

                    ShippingService.ShippingCost shippingCost = new ShippingService.ShippingCost();
                    shippingCost.setValue(cost.getCost().get(0).getValue());
                    shippingCost.setEtd(cost.getCost().get(0).getEtd());
                    service.setCost(shippingCost);

                    services.add(service);
                }
            }
        }
        return services;
    }

    // saya melakukan request ke API lagi untuk mendapatkan total berat dari semua item di keranjang diakrenakan tidak ingin mengubah model cart dan cartitem demi menghindari error
    private void calculateTotalWeightAsync(OnWeightCalculatedListener listener) {
        List<CartItem> cartItems = sessionManager.getCartItems();
        AtomicInteger totalWeight = new AtomicInteger(0);
        AtomicInteger processedItems = new AtomicInteger(0);

        for (CartItem item : cartItems) {
            ApiClient.getClient().getProductDetail(item.getProductId())
                    .enqueue(new Callback<ProductDetailResponse>() {
                        @Override
                        public void onResponse(Call<ProductDetailResponse> call, Response<ProductDetailResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Product product = response.body().getProduct();
                                totalWeight.addAndGet(product.getWeight() * item.getQuantity());
                            }

                            // Check if all items are processed
                            if (processedItems.incrementAndGet() == cartItems.size()) {
                                requireActivity().runOnUiThread(() ->
                                        listener.onWeightCalculated(totalWeight.get())
                                );
                            }
                        }

                        @Override
                        public void onFailure(Call<ProductDetailResponse> call, Throwable t) {
                            processedItems.incrementAndGet();
                            if (processedItems.get() == cartItems.size()) {
                                requireActivity().runOnUiThread(() ->
                                        listener.onWeightCalculated(totalWeight.get())
                                );
                            }
                        }
                    });
        }
    }

    interface OnWeightCalculatedListener {
        void onWeightCalculated(int totalWeight);
    }

    private void updateServiceDisplay() {
        if (selectedService != null) {
            String serviceText = String.format("%s - %s\nRp %,d (%s hari)",
                    selectedService.getService(),
                    selectedService.getDescription(),
                    selectedService.getCost().getValue(),
                    selectedService.getCost().getEtd());
            binding.textSelectedService.setText(serviceText);

            // Update shipping cost display
            binding.textShippingCost.setText(String.format("Ongkos Kirim (%s)     Rp %,d",
                    selectedCourier.getName(),
                    selectedService.getCost().getValue()));
            updateSummary();
        }
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

        // Add shipping cost if service is selected
        int shippingCost = (selectedService != null) ? selectedService.getCost().getValue() : 0;
        int totalPrice = subtotal + shippingCost;

        // Update UI
        binding.textTotalItems.setText(String.format("Total Harga (%d barang)     Rp %,d", items.size(), subtotal));
        if (selectedService != null) {
            binding.textShippingCost.setText(String.format("Ongkos Kirim (%s)     Rp %,d",
                    selectedCourier.getName(),
                    shippingCost));
        } else {
            binding.textShippingCost.setText("Ongkos Kirim     Rp 0");
        }
        binding.textPriceBottom.setText(String.format("Rp %,d", totalPrice));
    }

    private void showCourierSelectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_select_courier, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerCouriers);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        CourierAdapter adapter = new CourierAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setListener(courier -> {
            selectedCourier = courier;
            updateCourierDisplay();
            dialog.dismiss();
        });

        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getClient().getCouriers()
                .enqueue(new Callback<CourierResponse>() {
                    @Override
                    public void onResponse(Call<CourierResponse> call, Response<CourierResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setCouriers(response.body().getCouriers());
                        } else {
                            Toast.makeText(requireContext(),
                                    "Failed to load couriers",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CourierResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }

    private void updateCourierDisplay() {
        if (selectedCourier != null) {
            // Update text di card kurir
            binding.textSelectedCourier.setText(selectedCourier.getName());

            updateSummary();
        }
    }

    private void setupPaymentMethodSelection() {
        binding.paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCod) {
                selectedPaymentMethod = "cod";
            } else if (checkedId == R.id.radioMidtrans) {
                selectedPaymentMethod = "midtrans";
            }
        });
    }

    private void setupPayButton() {
        binding.btnPayNow.setOnClickListener(v -> {
            // Validation checks
            if (selectedAddress == null) {
                Toast.makeText(requireContext(), "Pilih alamat pengiriman terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCourier == null) {
                Toast.makeText(requireContext(), "Pilih kurir pengiriman terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedService == null) {
                Toast.makeText(requireContext(), "Pilih layanan pengiriman terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected payment method
            RadioGroup paymentGroup = binding.paymentMethodGroup;
            int selectedId = paymentGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Pilih metode pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

//            RadioButton selectedPayment = binding.getRoot().findViewById(selectedId);
//            String paymentMethod = selectedPayment.getTag().toString(); // "midtrans" or "cod"

            // Show loading
            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Memproses pesanan...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            createOrder(progressDialog);
        });
    }

    private void createOrder(ProgressDialog progressDialog) {
        progressDialog.setMessage("Memproses pesanan...");
        progressDialog.show();

        // Get selected payment method
        RadioGroup paymentGroup = binding.paymentMethodGroup;
        String paymentMethod = ((RadioButton) binding.getRoot()
                .findViewById(paymentGroup.getCheckedRadioButtonId())).getTag().toString();

        // Calculate total price
        List<CartItem> items = sessionManager.getCartItems();
        if (items.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Keranjang kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        int subtotal = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
        int totalPrice = subtotal + selectedService.getCost().getValue();

        // Prepare request body
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("user_id", sessionManager.getUserId());
        orderData.put("payment_method", paymentMethod);
        orderData.put("payment_status", "unpaid");
        orderData.put("shipping_address", selectedAddress.getFullAddress());
        orderData.put("shipping_city", selectedAddress.getCityName());
        orderData.put("shipping_province", selectedAddress.getProvinceName());
        orderData.put("shipping_postal_code", selectedAddress.getPostalCode());
        orderData.put("shipping_cost", selectedService.getCost().getValue());
        orderData.put("courier", selectedCourier.getCode());
        orderData.put("service", selectedService.getService());
        orderData.put("total_price", totalPrice);
        orderData.put("etd_days", selectedService.getCost().getEtd());
        orderData.put("status", "pending");
        orderData.put("items", items); // Add cart items to request

        ApiClient.getClient().createOrder(orderData).enqueue(new Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int orderId = response.body().getOrder().getId();

                    if ("cod".equals(selectedPaymentMethod)) {
                        // For COD
                        sessionManager.clearCart();
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Pesanan Berhasil Dibuat", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigate(R.id.navigation_cart);
                    } else {
                        // For Midtrans
                        createMidtransPayment(orderId, progressDialog);
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Gagal membuat pesanan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createMidtransPayment(int orderId, ProgressDialog progressDialog) {
        Map<String, String> paymentData = new HashMap<>();
        paymentData.put("order_id", String.valueOf(orderId));
        paymentData.put("user_id", sessionManager.getUserId());

        Log.d("Payment", "Creating Midtrans payment for order: " + orderId);

        ApiClient.getClient().createMidtransPayment(paymentData)
                .enqueue(new Callback<MidtransResponse>() {
                    @Override
                    public void onResponse(Call<MidtransResponse> call, Response<MidtransResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            MidtransResponse midtransResponse = response.body();
                            if (midtransResponse.getStatus() == 1 && midtransResponse.getData() != null) {
                                // Now get order details after Midtrans payment is created
                                getOrderDetails(orderId, progressDialog);
                            } else {
                                progressDialog.dismiss();
                                showError("Gagal membuat pembayaran Midtrans");
                            }
                        } else {
                            progressDialog.dismiss();
                            showError("Gagal membuat pembayaran");
                        }
                    }

                    @Override
                    public void onFailure(Call<MidtransResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        showError("Error: " + t.getMessage());
                    }
                });
    }

    private void getOrderDetails(int orderId, ProgressDialog progressDialog) {
        Log.d("OrderDetails", "Fetching order details for ID: " + orderId);

        ApiClient.getClient().getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    OrderDetailResponse detailResponse = response.body();
                    if (detailResponse.getStatus() == 1 && detailResponse.getOrder() != null) {
                        Order order = detailResponse.getOrder();
                        String paymentUrl = order.getPaymentUrl();
                        String paymentToken = order.getPaymentToken();

                        Log.d("OrderDetails", "Payment URL: " + paymentUrl);
                        Log.d("OrderDetails", "Payment Token: " + paymentToken);

                        if (paymentUrl != null && !paymentUrl.isEmpty()) {
                            // Clear cart after successful order
                            sessionManager.clearCart();

                            // Navigate to payment webview
                            Bundle args = new Bundle();
                            args.putString("payment_url", paymentUrl);
                            args.putString("transaction_id", paymentToken);
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.paymentWebViewFragment, args);
                        } else {
                            showError("Payment URL tidak ditemukan");
                        }
                    } else {
                        showError("Invalid order data");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        showError("Gagal mendapatkan detail order: " + errorBody);
                    } catch (IOException e) {
                        showError("Gagal mendapatkan detail order");
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                progressDialog.dismiss();
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (isAdded()) {
            Log.e("OrderDetails", message);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
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