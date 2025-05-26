package com.example.agress.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.agress.R;
import com.example.agress.adapter.ProductAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.ProductResponse;
import com.example.agress.databinding.FragmentProductBinding;
import com.example.agress.model.Product;

import java.util.List;

import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private ProductAdapter products;
    private ApiService apiService;
    private boolean isLoading = false;
    public String saveSearchQuery = null; // untuk menyimpan query pencarian
    private String categorySelect = null; // untuk memilih produk kategori
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categorySelect = getArguments().getString("category");
            saveSearchQuery = getArguments().getString("search_query");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);

        swipeRefreshLayout = binding.swipeRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh the content
            loadProducts();

            swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
        });

        setupRecyclerView();

        loadProducts();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        products = new ProductAdapter(requireContext());
        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewProducts.setAdapter(products);

        products.setProductsClickListener(product -> {
            Bundle bundle = new Bundle();
            bundle.putInt("product_id", product.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_product_detail, bundle);
        });
    }

    private void loadProducts() {
        if (isLoading) return;
        isLoading = true;

        apiService = ApiClient.getClient();
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                isLoading = false;
                binding.swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> productList = response.body().getProducts();
                    products.setProducts(productList);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                isLoading = false;
                binding.swipeRefreshLayout.setRefreshing(false);
                // Handle error - show toast or error message
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}