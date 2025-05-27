package com.example.agress.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private ProductAdapter products;
    private ApiService apiService;
    private boolean isLoading = false;
    private boolean isDestroyed = false; // Tambahkan flag ini
    public String saveSearchQuery = null; // untuk menyimpan query pencarian
    private String categorySelect = null; // untuk memilih produk kategori
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Product> allProducts = new ArrayList<>(); // Menyimpan semua produk untuk  filtering

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
        isDestroyed = false; // Reset flag saat fragment dibuat

        swipeRefreshLayout = binding.swipeRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh the content
            loadProducts();

            swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
        });

        // Check if there's a search query from arguments
        if (getArguments() != null && getArguments().containsKey("search_query")) {
            String searchQuery = getArguments().getString("search_query");
            binding.searchView.setQuery(searchQuery, false);
        }

        setupSearchView();
        setupRecyclerView();
        loadProducts();

        return binding.getRoot();
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        if (allProducts == null) return;

        if (query.isEmpty()) {
            products.setProducts(allProducts);
            return;
        }

        List<Product> filteredList = allProducts.stream()
                .filter(product ->
                        product.getProductName().toLowerCase().contains(query.toLowerCase()) ||
                                product.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                                product.getCategory().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());

        products.setProducts(filteredList);
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

                // Check if fragment is destroyed
                if (isDestroyed || binding == null) return;

                binding.swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body().getProducts();
                    products.setProducts(allProducts);

                    // Filter jika ada query yang aktif
                    String currentQuery = binding.searchView.getQuery().toString();
                    if (!currentQuery.isEmpty()) {
                        filterProducts(currentQuery);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                isLoading = false;

                // Check if fragment is destroyed
                if (isDestroyed || binding == null) return;

                binding.swipeRefreshLayout.setRefreshing(false);
                // Handle error - show toast or error message
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true; // Set flag saat fragment dihancurkan
        binding = null;
    }
}