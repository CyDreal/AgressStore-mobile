package com.example.agress.ui.cart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.CourierResponse;
import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.api.response.ShippingCostResponse;
import com.example.agress.databinding.FragmentCheckoutBinding;
import com.example.agress.model.Address;
import com.example.agress.model.CartItem;
import com.example.agress.model.Courier;
import com.example.agress.model.Product;
import com.example.agress.model.ShippingService;
import com.example.agress.utils.SessionManager;

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

            RadioButton selectedPayment = binding.getRoot().findViewById(selectedId);
            String paymentMethod = selectedPayment.getTag().toString(); // "midtrans" or "cod"

            if (selectedId == R.id.radioCod) {
                createOrder();
            } else {
                Toast.makeText(requireContext(), "Maaf, pembayaran via Midtrans sedang dalam maintenance", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createOrder() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Memproses pesanan...");
        progressDialog.show();

        // Calculate total price
        List<CartItem> items = sessionManager.getCartItems();
        int subtotal = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
        int totalPrice = subtotal + selectedService.getCost().getValue();

        // Prepare request body
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("user_id", sessionManager.getUserId());
        orderData.put("payment_method", "cod");
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

        // Make API call
        ApiClient.getClient().createOrder(orderData)
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            // Clear local cart in SessionManager only
                            sessionManager.clearCart();

                            Toast.makeText(requireContext(), "Pesanan berhasil dibuat", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Gagal membuat pesanan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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