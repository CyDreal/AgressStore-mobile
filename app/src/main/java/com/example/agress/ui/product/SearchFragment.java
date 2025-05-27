package com.example.agress.ui.product;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import com.example.agress.R;
import com.example.agress.adapter.SearchSuggestionAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.ProductResponse;
import com.example.agress.databinding.FragmentSearchBinding;
import com.example.agress.model.Product;
import com.example.agress.utils.SearchHistoryManager;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private SearchSuggestionAdapter adapter;
    private List<Product> allProducts;
    private SearchHistoryManager searchHistoryManager;
    private ArrayAdapter<String> historyAdapter;
    private ApiService apiService;
    private boolean isDestroyed = false;
    private Call<?> activeCall; // Track active network call

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        // Hide bottom navigation
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.GONE);

        // Inisialisasi SearchHistoryManager di awal
        searchHistoryManager = new SearchHistoryManager(requireContext());

        setupBackButton();
        setupRecyclerView();
        setupSearchView();
        setupHistoryList();
        loadProducts();

        // Show history list by default
        binding.recyclerViewSuggestions.setVisibility(View.GONE);
        binding.listViewHistory.setVisibility(View.VISIBLE);
        updateHistoryList();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new SearchSuggestionAdapter(requireContext());
        binding.recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewSuggestions.setAdapter(adapter);

        adapter.setOnSuggestionClickListener(product -> {
            Bundle bundle = new Bundle();
            bundle.putInt("product_id", product.getId());
            // Navigate directly to product detail
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_product_detail, bundle);
        });
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            // Navigate back to dashboard
            Navigation.findNavController(v)
                    .popBackStack(R.id.navigation_dashboard, false);
        });
    }

    private void setupSearchView() {
        binding.searchView.requestFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    // Save to history
                    searchHistoryManager.addQuery(query);

                    // Navigate to ProductFragment with search query
                    Bundle bundle = new Bundle();
                    bundle.putString("search_query", query);
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_search_to_product, bundle);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    binding.recyclerViewSuggestions.setVisibility(View.GONE);
                    binding.listViewHistory.setVisibility(View.VISIBLE);
                    updateHistoryList();
                } else {
                    binding.recyclerViewSuggestions.setVisibility(View.VISIBLE);
                    binding.listViewHistory.setVisibility(View.GONE);
                    filterProducts(newText);
                }
                return true;
            }
        });
    }

    private void setupHistoryList() {
        historyAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                searchHistoryManager.getSearchHistory());

        binding.listViewHistory.setAdapter(historyAdapter);
        binding.listViewHistory.setOnItemClickListener((parent, view, position, id) -> {
            String query = historyAdapter.getItem(position);
            binding.searchView.setQuery(query, true);
        });
    }

    private void updateHistoryList() {
        historyAdapter.clear();
        historyAdapter.addAll(searchHistoryManager.getSearchHistory());
        historyAdapter.notifyDataSetChanged();
    }

    private void loadProducts() {
        if (isDestroyed) return;

        // Cancel any existing call
        if (activeCall != null) {
            activeCall.cancel();
        }

        ApiService apiService = ApiClient.getClient();
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                // Check if fragment is still active
                if (isDestroyed || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body().getProducts();
                    // Only filter if searchView exists and fragment is active
                    if (binding != null && binding.searchView != null) {
                        String currentQuery = binding.searchView.getQuery().toString();
                        filterProducts(currentQuery);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                // Check if fragment is still active
                if (isDestroyed || binding == null) return;

                if (!call.isCanceled()) {
                    // Handle error - show toast or error message
                }
            }
        });
    }

    private void filterProducts(String query) {
        if (allProducts == null) return;

        if (query.isEmpty()) {
            adapter.setSuggestions(allProducts);
            return;
        }

        List<Product> filteredList = allProducts.stream()
                .filter(product ->
                    product.getProductName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    product.getCategory().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());

        adapter.setSuggestions(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;

        // Cancel any pending network calls
        if (activeCall != null) {
            activeCall.cancel();
        }

        // Show bottom navigation when leaving fragment
        if (requireActivity().findViewById(R.id.nav_view) != null) {
            requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
        }
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }
}