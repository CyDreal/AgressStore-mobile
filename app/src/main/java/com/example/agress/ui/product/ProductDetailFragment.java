package com.example.agress.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        setupImageSlider();
        setupBackButton();
        loadProductDetail();

        return binding.getRoot();
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
        apiService = ApiClient.getClient();
        apiService.getProductDetail(productId).enqueue(new Callback<ProductDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductDetailResponse> call,
                                 @NonNull Response<ProductDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getProduct();
                    displayProductDetails(product);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDetailResponse> call, @NonNull Throwable t) {
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

        // Set status chip
        binding.chipStatus.setText(product.getStatus());
        if ("available".equals(product.getStatus())) {
            binding.chipStatus.setChipBackgroundColorResource(R.color.success);
        } else {
            binding.chipStatus.setChipBackgroundColorResource(R.color.error);
        }

        // Handle add to cart button
        binding.btnAddToCart.setEnabled(product.getStock() > 0);
        binding.btnAddToCart.setOnClickListener(v -> {
            // Handle add to cart logic
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}