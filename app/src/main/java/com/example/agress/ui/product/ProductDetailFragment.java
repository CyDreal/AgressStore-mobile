package com.example.agress.ui.product;

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

import com.bumptech.glide.Glide;
import com.example.agress.R;
import com.example.agress.adapter.ProductImageAdapter;
import com.example.agress.adapter.ProductThumbnailAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.ProductDetailResponse;
import com.example.agress.databinding.FragmentProductDetailBinding;
import com.example.agress.model.Product;
import com.example.agress.model.ProductImage;

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
    private boolean isDestroyed = false; // Tambahkan flag ini
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

        // Handle add to cart button click only if product is available
        binding.btnAddToCart.setOnClickListener(isAvailable ? v -> {
            // Handle add to cart logic
        } : null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
        if (activeCall != null) {
            activeCall.cancel();
        }
        binding = null;
    }
}