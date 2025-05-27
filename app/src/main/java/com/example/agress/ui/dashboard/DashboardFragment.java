package com.example.agress.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.agress.R;
import com.example.agress.adapter.CategoryAdapter;
import com.example.agress.adapter.ProductAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.ProductResponse;
import com.example.agress.databinding.FragmentDashboardBinding;
import com.example.agress.model.Category;
import com.example.agress.model.Product;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageSlider imageSlider;
    private ProductAdapter tabProductAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private TabLayout.OnTabSelectedListener tabSelectedListener;
    private boolean isTabLayoutInitialized = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        // Setup base components
        setupSearchButton();
        setupImageSlider();
        setupCategories();
        setupRecyclerView(); // Separate RecyclerView setup

        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshContent();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        tabProductAdapter = new ProductAdapter(requireContext());
        binding.tabProductRecyclerView.setLayoutManager(
                new GridLayoutManager(requireContext(), 2)
        );
        binding.tabProductRecyclerView.setAdapter(tabProductAdapter);

        tabProductAdapter.setProductsClickListener(product -> {
            Bundle bundle = new Bundle();
            bundle.putInt("product_id", product.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_product_detail, bundle);
        });
    }

    private void refreshContent() {
        allProducts.clear();
        if (tabProductAdapter != null) {
            tabProductAdapter.setProducts(new ArrayList<>());
        }
        loadProducts();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always reload data when returning to fragment
        loadProducts();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isTabLayoutInitialized) {
            setupTabLayout();
            isTabLayoutInitialized = true;
        }
        loadProducts();
    }

    private void setupViews() {
        // Setup RecyclerView
        if (tabProductAdapter == null) {
            tabProductAdapter = new ProductAdapter(requireContext());
            binding.tabProductRecyclerView.setLayoutManager(
                    new GridLayoutManager(requireContext(), 2)
            );
            binding.tabProductRecyclerView.setAdapter(tabProductAdapter);
        }

        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshContent);

        // Setup TabLayout
        setupTabLayout();
    }

    private void setupTabLayout() {
        TabLayout tabLayout = binding.productTabLayout;

        // Clear existing tabs first
        tabLayout.removeAllTabs();

        // Remove existing listener if any
        if (tabSelectedListener != null) {
            tabLayout.removeOnTabSelectedListener(tabSelectedListener);
        }

        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("Best Seller"));
        tabLayout.addTab(tabLayout.newTab().setText("Recommended"));
//        tabLayout.addTab(tabLayout.newTab().setText("New Arrivals"));

        // Create and set tab listener
        tabSelectedListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!allProducts.isEmpty()) {
                    filterProductsByTab(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        };
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
    }

    private void loadProducts() {
        if (!isAdded() || binding == null) return;

        binding.swipeRefreshLayout.setRefreshing(true);
        ApiClient.getClient().getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call,
                                   @NonNull Response<ProductResponse> response) {
                if (!isAdded() || binding == null) return;

                binding.swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body().getProducts();
                    if (!allProducts.isEmpty()) {
                        filterProductsByTab(binding.productTabLayout.getSelectedTabPosition());
                    }
                } else {
                    showError("Failed to load products");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                binding.swipeRefreshLayout.setRefreshing(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        // Show error message using Snackbar or Toast
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void filterProductsByTab(int tabPosition) {
        if (allProducts.isEmpty()) return;

        List<Product> filteredProducts = new ArrayList<>(allProducts);

        switch (tabPosition) {
            case 0: // Best Sellers
                filteredProducts.sort((p1, p2) ->
                        Integer.compare(p2.getPurchaseQuantity(), p1.getPurchaseQuantity())
                );
                break;

            case 1: // Recommended (now sorted by view count)
                filteredProducts.sort((p1, p2) ->
                        Integer.compare(p2.getViewCount(), p1.getViewCount())
                );
                break;

            case 2: // New Arrivals
                filteredProducts = getLastMonthProducts(filteredProducts);
                if (!filteredProducts.isEmpty()) {
                    filteredProducts.sort((p1, p2) -> {
                        String date1 = p1.getCreatedAt();
                        String date2 = p2.getCreatedAt();
                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;
                        return date2.compareTo(date1);
                    });
                }
                break;
        }

        // Take top N products
        int maxItems = 6;
        if (filteredProducts.size() > maxItems) {
            filteredProducts = filteredProducts.subList(0, maxItems);
        }

        tabProductAdapter.setProducts(filteredProducts);
    }

    private List<Product> getLastMonthProducts(List<Product> products) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            sdf.setLenient(true);

            Calendar thirtyDaysAgo = Calendar.getInstance();
            thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);

            return products.stream()
                    .filter(product -> {
                        try {
                            String dateStr = product.getCreatedAt();
                            if (dateStr == null) return false;

                            // Remove decimal places from seconds if present
                            dateStr = dateStr.replaceAll("\\.\\d+", "");
                            Date createdDate = sdf.parse(dateStr);
                            return createdDate != null && createdDate.after(thirtyDaysAgo.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void setupSearchButton() {
        binding.searchContainer.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_to_search)
        );
    }

    private void setupImageSlider() {
        imageSlider = binding.imageSliderBanner;
        ArrayList<SlideModel> slideModels = new ArrayList<>();

        // menggunakan hardcoded image langsung diambil dari drawable
        slideModels.add(new SlideModel(R.drawable.banner1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner4, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner5, ScaleTypes.FIT));

        // Menggunakan imageSlider yang sudah diinisialisasi
        binding.imageSliderBanner.setImageList(slideModels, ScaleTypes.FIT);
    }

    private void setupCategories() {
        List<Category> categories = Arrays.asList(
                new Category(R.drawable.ic_laptop, "Laptop"),
                new Category(R.drawable.ic_phone, "Phone"),
                new Category(R.drawable.ic_console, "Console")
        );

        CategoryAdapter adapter = new CategoryAdapter(categories);
        RecyclerView recyclerView = binding.categoryRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Category category = categories.get(position);
            // Navigate to ProductFragment with category
            Bundle bundle = new Bundle();
            bundle.putString("category", category.getName());
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_product, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        if (binding != null && tabSelectedListener != null) {
            binding.productTabLayout.removeOnTabSelectedListener(tabSelectedListener);
        }
        isTabLayoutInitialized = false;
        allProducts.clear();
        tabSelectedListener = null;
        super.onDestroyView();
        binding = null;
    }
}