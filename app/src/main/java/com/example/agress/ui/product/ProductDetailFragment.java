package com.example.agress.ui.product;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.databinding.FragmentProductDetailBinding;
import com.example.agress.model.CartItem;
import com.example.agress.model.Product;
import com.example.agress.model.ProductImage;
import com.example.agress.utils.SessionManager;
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
    private Call<?> activeCall; // Menangani error yang terjadi karena race condition antara network
                                // call dan lifecycle fragment. Saat user cepat kembali sebelum response selesai,
                                // binding sudah null tapi callback masih mencoba mengakses view.

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt("product_id");
        }
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
        if (isAvailable && product.getStock() > 0) {
            setupAddToCart(product);
        }
    }

    private void setupAddToCart(Product product) {
        binding.btnAddToCart.setOnClickListener(v -> {
            if (sessionManager.isGuest()) {
                showLoginRequiredDialog();
                return;
            }

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                // Cek apakah produk sudah ada di cart
                List<CartItem> currentCart = sessionManager.getCartItems();
                boolean productExists = false;

                for (CartItem item : currentCart) {
                    if (item.getProductId() == product.getId()) {
                        // Produk sudah ada, tambah quantity
                        if (item.getQuantity() < item.getStock()) {
                            item.setQuantity(item.getQuantity() + 1);
                            sessionManager.addToCart(item);
                            showUpdateCartSuccess();
                        } else {
                            Snackbar.make(binding.getRoot(),
                                    "Maximum stock reached",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                        productExists = true;
                        break;
                    }
                }

                if (!productExists) {
                    // Produk belum ada, tambah baru
                    CartItem cartItem = new CartItem(
                            product.getId(),
                            product.getProductName(),
                            product.getPrice(),
                            1,
                            product.getImages().get(0).getImageUrl(),
                            product.getStock()
                    );
                    sessionManager.addToCart(cartItem);
                    showAddToCartSuccess();
                }
            }
        });
    }

    private void showUpdateCartSuccess() {
        Snackbar.make(binding.getRoot(),
                        "Cart quantity updated",
                        Snackbar.LENGTH_SHORT)
                .setAction("View Cart", v -> {
                    Navigation.findNavController(v)
                            .navigate(R.id.navigation_checkout);
                })
                .show();
    }

    private void showAddToCartSuccess() {
        Snackbar.make(binding.getRoot(),
                        "Product added to cart",
                        Snackbar.LENGTH_SHORT)
                .setAction("View Cart", v -> {
                    Navigation.findNavController(v)
                            .navigate(R.id.navigation_checkout);
                })
                .show();
    }

    private void showLoginRequiredDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Login Required")
                .setMessage("You need to login to add items to cart")
                .setPositiveButton("Login", (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), UserIdentifyActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
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