package com.example.agress.ui.product;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.agress.R;
import com.example.agress.UserIdentifyActivity;
import com.example.agress.adapter.ProductImageAdapter;
import com.example.agress.adapter.ProductThumbnailAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.CartListResponse;
import com.example.agress.api.response.CartResponse;
import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.databinding.FragmentProductDetailBinding;
import com.example.agress.model.Cart;
import com.example.agress.model.CartItem;
import com.example.agress.model.Product;
import com.example.agress.model.ProductImage;
import com.example.agress.utils.CartManager;
import com.example.agress.utils.SessionManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailFragment extends Fragment {
    private FragmentProductDetailBinding binding;
    private ApiService apiService;
    private int productId;
    private ProductImageAdapter imageAdapter;
    private ProductThumbnailAdapter thumbnailAdapter;
    private boolean isDestroyed = false;
    private SessionManager sessionManager;
    private CartManager cartManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt("product_id");
        }
        sessionManager = new SessionManager(requireContext());
        cartManager = new CartManager(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        sessionManager = new SessionManager(requireContext());

        // Hide bottom navigation
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.GONE);

        isDestroyed = false;

        setupImageSlider();
        setupBackButton();
        loadProductDetail();
        updateViewCount();

        return binding.getRoot();
    }

    private void updateViewCount() {
        apiService = ApiClient.getClient();
        apiService.updateViewCount(productId).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Reload product details to get updated view count
                    loadProductDetail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                // Handle error silently
            }
        });
    }

    private void setupImageSlider() {
        imageAdapter = new ProductImageAdapter(requireContext());
        thumbnailAdapter = new ProductThumbnailAdapter(requireContext(), binding.viewPagerImages);

        binding.viewPagerImages.setAdapter(imageAdapter);
        binding.recyclerViewThumbnails.setAdapter(thumbnailAdapter);
        binding.recyclerViewThumbnails.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        binding.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Update thumbnail selection based on the current image
                thumbnailAdapter.updateSelection(position);
                binding.recyclerViewThumbnails.smoothScrollToPosition(position);
            }
        });
    }

    private void setupBackButton() {
        binding.backButton.setOnClickListener(v ->
            Navigation.findNavController(v).navigateUp()
        );
    }

    private void loadProductDetail() {
        if (isDestroyed) return;

        apiService = ApiClient.getClient();
        apiService.getProductDetail(productId).enqueue(new Callback<ProductDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductDetailResponse> call,
                                   @NonNull Response<ProductDetailResponse> response) {
                if (isDestroyed || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getProduct();
                    displayProductDetails(product);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDetailResponse> call, @NonNull Throwable t) {
                if (isDestroyed || binding == null) return;
                // Handle error
            }
        });
    }

    private void displayProductDetails(Product product) {

        if (product.getStock() > 0) {
            setupAddToCartButton(product);
        } else {
            binding.btnAddToCart.setEnabled(false);
            binding.btnAddToCart.setText("Out of Stock");
        }
        // Display main image (image_order = 0)
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // Sort images based on image_order
            List<ProductImage> sortedImages = new ArrayList<>(product.getImages());
            Collections.sort(sortedImages, (a, b) -> Integer.compare(a.getImageOrder(), b.getImageOrder()));

            imageAdapter.setImages(product.getImages());
            thumbnailAdapter.setImages(product.getImages());
        }

        // Set text fields
        binding.tvProductName.setText(product.getProductName());
        binding.tvPrice.setText(String.format("Rp %,d", product.getPrice()));
        binding.tvCategory.setText(product.getCategory());
        binding.tvStock.setText(String.valueOf(product.getStock()));
        binding.tvVisitCount.setText(String.valueOf(product.getViewCount()));
        binding.tvDescription.setText(product.getDescription());
        // Update view count display
        binding.tvVisitCount.setText(String.valueOf(product.getViewCount()));
        // Add purchased quantity display
        binding.tvPurchased.setText(String.valueOf(product.getPurchaseQuantity()));

        // Handle product status
        boolean isAvailable = "available".equals(product.getStatus());

        // Update status chip
        binding.chipStatus.setText(product.getStatus());
        binding.chipStatus.setChipBackgroundColorResource(
                isAvailable ? R.color.success : R.color.error
        );

        // Update add to cart button state
        binding.btnAddToCart.setEnabled(isAvailable && product.getStock() > 0);
        binding.btnAddToCart.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(),
                        isAvailable ? R.color.primary : R.color.disabled)
        ));

        // Show/hide sold out text
        binding.tvSoldOut.setVisibility(isAvailable ? View.GONE : View.VISIBLE);

        // Setup add to cart functionality
//        binding.btnAddToCart.setOnClickListener(v -> {
//            if (isAvailable && product.getStock() > 0) {
//                setupAddToCartButton(product);
//            }
//        });
    }

//    private void addToCart(Product product) {
//        if (isDestroyed || binding == null) return;
//
//        binding.progressBar.setVisibility(View.VISIBLE);
//        binding.btnAddToCart.setEnabled(false);
//
//        if (product != null) {
//            CartItem cartItem = new CartItem(
//                    0, // id (0 for new items)
//                    product.getId(),
//                    product.getProductName(),
//                    product.getPrice(),
//                    1, // Default quantity
//                    product.getImages() != null && !product.getImages().isEmpty() ?
//                            product.getImages().get(0).getImageUrl() : "",
//                    product.getStock()
//            );
//
//            if (!sessionManager.isLoggedIn()) {
//                // For guest users
//                cartManager.addToCart(cartItem);
//                showToast("Added to cart");
//                updateUI();
//            } else {
//                // For logged-in users
//                apiService.addToCart(sessionManager.getUserId(), product.getId(), 1)
//                        .enqueue(new Callback<CartResponse>() {
//                            @Override
//                            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
//                                if (isDestroyed || binding == null) return;
//                                if (response.isSuccessful()) {
//                                    showToast("Added to cart");
//                                } else {
//                                    showToast("Failed to add to cart");
//                                }
//                                updateUI();
//                            }
//
//                            @Override
//                            public void onFailure(Call<CartResponse> call, Throwable t) {
//                                if (isDestroyed || binding == null) return;
//                                showToast("Network error");
//                                updateUI();
//                            }
//                        });
//            }
//        } else {
//            // Handle case where product is null
//            binding.btnAddToCart.setEnabled(true);
//            Toast.makeText(requireContext(), "Product information is not available", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void setupAddToCartButton(Product product) {
        binding.btnAddToCart.setOnClickListener(v -> {
            if (sessionManager.isGuest()) {
                // Handle guest cart
                handleGuestAddToCart(product);
            } else {
                // Handle logged in user cart
                handleUserAddToCart(product);
            }
        });
    }

    private void handleGuestAddToCart(Product product) {
        CartManager cartManager = new CartManager(requireContext());

        // Check if product already exists in cart
        if (cartManager.hasItem(product.getId())) {
            // Get current quantity and increment
            CartItem existingItem = cartManager.getCartMap().get(product.getId());
            if (existingItem != null) {
                int newQuantity = existingItem.getQuantity() + 1;
                if (newQuantity > product.getStock()) {
                    Toast.makeText(requireContext(),
                            "Jumlah melebihi stok yang tersedia",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                existingItem.setQuantity(newQuantity);
                cartManager.updateCartItem(existingItem);
                Toast.makeText(requireContext(),
                        "Jumlah berhasil ditambah",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add new item to cart
            String imageUrl = product.getImages() != null && !product.getImages().isEmpty() ?
                    product.getImages().get(0).getImageUrl() : "";

            CartItem newItem = new CartItem(
                    0,
                    product.getId(),
                    product.getProductName(),
                    product.getPrice(),
                    1, // Default quantity
                    product.getImages() != null && !product.getImages().isEmpty() ?
                            product.getImages().get(0).getImageUrl() : "",
                    product.getStock()
            );

            cartManager.addToCart(newItem);
            Toast.makeText(requireContext(),
                    "Produk ditambahkan ke keranjang",
                    Toast.LENGTH_SHORT).show();
        }

        // Update cart count in UI if needed
        updateCartBadge();
    }

    private void handleUserAddToCart(Product product) {
        // Show loading
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Menambahkan ke keranjang...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // First, get the current cart to check if product exists
        ApiService apiService = ApiClient.getClient();
        apiService.getUserCarts(sessionManager.getUserId())
                .enqueue(new Callback<CartListResponse>() {
                    @Override
                    public void onResponse(Call<CartListResponse> call, Response<CartListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Check if product exists in cart
                            boolean productExists = false;
                            int existingQuantity = 0;
                            String cartItemId = null;

                            for (Cart cart : response.body().getCarts()) {
                                if (cart.getProductId() == product.getId()) {
                                    productExists = true;
                                    existingQuantity = cart.getQuantity();
                                    cartItemId = String.valueOf(cart.getId());
                                    break;
                                }
                            }

                            if (productExists) {
                                // Update existing cart item
                                updateCartItemQuantity(cartItemId, existingQuantity + 1, product, progressDialog);
                            } else {
                                // Add new item to cart
                                addNewCartItem(product, progressDialog);
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(),
                                    "Gagal memuat keranjang",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CartListResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(),
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCartItemQuantity(String cartItemId, int newQuantity, Product product, ProgressDialog progressDialog) {
        if (newQuantity > product.getStock()) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(),
                    "Jumlah melebihi stok yang tersedia",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Since the API doesn't have an update endpoint, we'll remove and re-add
        // This is not ideal but works with the current API
        ApiService apiService = ApiClient.getClient();

        // First remove the existing item
        apiService.removeFromCart(cartItemId).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                // Then add it back with the new quantity
                addNewCartItem(product, progressDialog, newQuantity);
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(),
                        "Gagal memperbarui keranjang",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewCartItem(Product product, ProgressDialog progressDialog) {
        addNewCartItem(product, progressDialog, 1);
    }

    private void addNewCartItem(Product product, ProgressDialog progressDialog, int quantity) {
        ApiService apiService = ApiClient.getClient();
        apiService.addToCart(
                sessionManager.getUserId(),
                product.getId(),
                quantity
        ).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(),
                            quantity > 1 ? "Jumlah berhasil ditambah" : "Produk ditambahkan ke keranjang",
                            Toast.LENGTH_SHORT).show();
                    updateCartBadge();
                } else {
                    Toast.makeText(requireContext(),
                            "Gagal menambahkan ke keranjang",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(),
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateCartBadge() {
        // Implement your cart badge update logic here
        // For example, if you're using BottomNavigationView with a badge:

//        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
//        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.navigation_cart);
//        CartManager cartManager = new CartManager(requireContext());
//        int itemCount = cartManager.getCartItemCount();
//        if (itemCount > 0) {
//            badge.setNumber(itemCount);
//            badge.setVisible(true);
//        } else {
//            badge.setVisible(false);
//        }

    }

    private void updateUI() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.btnAddToCart.setEnabled(true);
    }

    private void showLoading() {
        if (binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnAddToCart.setEnabled(false);
        }
    }

    private void hideLoading() {
        if (binding != null && !isDestroyed) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnAddToCart.setEnabled(true);
        }
    }

    private void showToast(String message) {
        if (getContext() != null && !isDestroyed) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
        // Kembalikan visibility bottom navigation saat fragment dihancurkan
        if (getActivity() != null) {
            getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
        }
        binding = null;
    }
}