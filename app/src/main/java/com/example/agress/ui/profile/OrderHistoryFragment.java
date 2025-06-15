package com.example.agress.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.agress.R;
import com.example.agress.adapter.OrderAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.model.Order;
import com.example.agress.api.response.OrderResponse;
import com.example.agress.databinding.FragmentOrderHistoryBinding;
import com.example.agress.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryFragment extends Fragment {
    private FragmentOrderHistoryBinding binding;
    private BottomNavigationView bottomNav;
    private OrderAdapter orderAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SessionManager
        sessionManager = new SessionManager(requireContext());

        // Initialize API service
        apiService = ApiClient.getClient();

        // Initialize adapter
        orderAdapter = new OrderAdapter();
        binding.recyclerOrders.setAdapter(orderAdapter);
        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set click listener for back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        // Hide bottom navigation
        hideBottomNavigation();

        // Load orders
        loadOrders();

        // Setup swipe refresh
        binding.swipeRefresh.setOnRefreshListener(this::loadOrders);
    }

    private boolean hasOrders() {
        // TODO: Implement your logic to check if there are orders
        // Example: return orderList != null && !orderList.isEmpty();
        return false; // Temporary return false to test empty state
    }

    private void loadOrders() {
        binding.swipeRefresh.setRefreshing(true);
        binding.recyclerOrders.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);

        // Get user ID from session
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) {
            // Handle not logged in state
            binding.swipeRefresh.setRefreshing(false);
            binding.emptyState.setVisibility(View.VISIBLE);
            return;
        }

        apiService.getUserOrders(Integer.parseInt(userId)).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body().getOrders();
                    if (orders != null && !orders.isEmpty()) {
                        orderAdapter.setOrders(orders);
                        binding.recyclerOrders.setVisibility(View.VISIBLE);
                        binding.emptyState.setVisibility(View.GONE);
                    } else {
                        binding.recyclerOrders.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.recyclerOrders.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                binding.recyclerOrders.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideBottomNavigation() {
        bottomNav = requireActivity().findViewById(R.id.nav_view);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void showBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show bottom navigation when leaving settings
        showBottomNavigation();
        binding = null;
    }
}