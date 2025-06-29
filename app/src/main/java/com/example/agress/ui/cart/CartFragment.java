package com.example.agress.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agress.R;
import com.example.agress.UserIdentifyActivity;
import com.example.agress.adapter.CartAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.CartListResponse;
import com.example.agress.api.response.CartResponse;
import com.example.agress.databinding.FragmentCartBinding;
import com.example.agress.model.Cart;
import com.example.agress.model.CartItem;
import com.example.agress.utils.CartManager;
import com.example.agress.utils.CurrencyFormatter;
import com.example.agress.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartAdapter.CartItemListener {

    private FragmentCartBinding binding;
    private SessionManager sessionManager;
    private CartAdapter cartAdapter;
    private CartManager cartManager;
    private List<CartItem> cartItems = new ArrayList<>();
    private Boolean isLoading = false;
    private ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        cartManager = new CartManager(requireContext());
        apiService = ApiClient.getClient();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupCheckoutButton();
        loadCartItems();

        // Set up swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadCartItems);
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

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(
                cartItems,
                new CartAdapter.CartItemListener() {
                    @Override
                    public void onQuantityChanged(CartItem item, int newQuantity) {
                        if (sessionManager.isLoggedIn()) {
                            // This will be handled by the adapter
                        } else {
                            updateCartItemLocally(item, newQuantity);
                        }
                    }

                    @Override
                    public void onRemoveItem(CartItem item) {
                        if (!sessionManager.isLoggedIn()) {
                            cartManager.removeFromCart(item.getProductId());
                            loadLocalCart();
                        }
                        // For logged in users, the removal is handled by the adapter
                    }

                    @Override
                    public void showLoading() {
                        binding.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void hideLoading() {
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void showError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                },
                requireContext(),
                sessionManager.isLoggedIn(),
                apiService,
                sessionManager.getUserId(),
                this
        );

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        if (isLoading) return;

        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.swipeRefreshLayout.setRefreshing(true);

        if (sessionManager.isLoggedIn()) {
            loadCartFromServer();
        } else {
            loadLocalCart();
        }
    }

    private void loadLocalCart() {
        cartItems.clear();
        cartItems.addAll(cartManager.getCartItems());
        updateUI();

        isLoading = false;
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private void loadCartFromServer() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) {
            showError("User not logged in");
            return;
        }

        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.swipeRefreshLayout.setRefreshing(true);

        apiService.getUserCarts(userId).enqueue(new Callback<CartListResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartListResponse> call,
                                   @NonNull Response<CartListResponse> response) {
                if (!isAdded()) return;

                isLoading = false;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    cartItems.clear();
                    for (Cart cart : response.body().getCarts()) {
                        CartItem cartItem = cart.toCartItem();
                        if (cartItem != null) {
                            cartItems.add(cartItem);
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateUI();
                } else {
                    showError("Failed to load cart");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartListResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;

                isLoading = false;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                showError("Network error");
            }
        });
    }

    private void updateCartItemLocally(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            cartManager.removeFromCart(item.getProductId());
            cartItems.removeIf(cartItem -> cartItem.getProductId() == item.getProductId());
        } else {
            item.setQuantity(newQuantity);
            cartManager.updateCartItem(item);
        }
        updateUI();
    }

    private void updateCartItemOnServer(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            removeItemFromServer(item);
            return;
        }

        // Update quantity on server
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) {
            showError("User not logged in");
            return;
        }

        ApiService apiService = ApiClient.getClient();
        apiService.addToCart(userId, item.getProductId(), newQuantity)
                .enqueue(new Callback<CartResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            // Update local list
                            for (CartItem cartItem : cartItems) {
                                if (cartItem.getProductId() == item.getProductId()) {
                                    cartItem.setQuantity(newQuantity);
                                    break;
                                }
                            }
                            updateUI();
                        } else {
                            showError("Failed to update cart");
                            // Reload cart to sync with server
                            loadCartItems();
                        }
                    }


                    @Override
                    public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showError("Network error: " + t.getMessage());
                        // Reload cart to sync with server
                        loadCartItems();
                    }
                });
    }

    private void removeItemFromServer(CartItem item) {
        // Create a final copy of the item to use in the lambda
        final CartItem itemToRemove = (item.getId() > 0) ? item : findCartItemByProductId(item.getProductId());

        if (itemToRemove == null) {
            // If we can't find the item, just remove by product ID as fallback
            cartItems.removeIf(ci -> ci.getProductId() == item.getProductId());
            updateUI();
            return;
        }

        // Now we can safely use itemToRemove in the lambda
        final int itemId = itemToRemove.getId();
        apiService.removeFromCart(String.valueOf(itemId))
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 200) {
                            cartItems.removeIf(ci -> ci.getId() == itemId);
                            updateUI();
                        } else {
                            showError("Failed to remove item");
                            loadCartItems(); // Reload to sync with server
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showError("Network error: " + t.getMessage());
                        loadCartItems();
                    }
                });
    }

    private CartItem findCartItemByProductId(int productId) {
        for (CartItem item : cartItems) {
            if (item.getProductId() == productId) {
                return item;
            }
        }
        return null;
    }

    private void showRemoveItemDialog(CartItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove Item")
                .setMessage("Are you sure you want to remove this item from your cart?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (sessionManager.isLoggedIn()) {
                        removeItemFromServer(item);
                    } else {
                        cartManager.removeFromCart(item.getProductId());
                        cartItems.removeIf(cartItem -> cartItem.getProductId() == item.getProductId());
                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void updateUI() {
        if (!isAdded()) return;

        cartAdapter.notifyDataSetChanged();

        // Calculate total
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }

        binding.textTotalPrice.setText(CurrencyFormatter.formatRupiah(total));

        // Show empty state if cart is empty
        if (cartItems.isEmpty()) {
            binding.btnCheckout.setEnabled(false);
        } else {
            binding.btnCheckout.setEnabled(true);
        }
    }

    private void setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                if (cartItems.isEmpty()) {
                    showError("Your cart is empty");
                    return;
                }
                Navigation.findNavController(v).navigate(R.id.action_cart_to_checkout);
            } else {
                showLoginRequiredDialog();
            }
        });
    }


    private void updateTotalPrice(List<CartItem> items) {
        int total = 0;
        for (CartItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        binding.textTotalPrice.setText(String.format("Total: Rp %,d", total));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (sessionManager.isLoggedIn()) {
            updateCartItemOnServer(item, newQuantity);
        } else {
            // Update in local cart
            cartManager.addToCart(new CartItem(
                    0, // id (0 for new items)
                    item.getProductId(),
                    item.getProductName(),
                    item.getPrice(),
                    newQuantity,
                    item.getImageUrl(),
                    item.getStock()
            ));
            loadCartItems(); // Refresh the cart
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        if (sessionManager.isLoggedIn()) {
            removeItemFromServer(item);
        } else {
            // remove from local cart
            cartManager.removeFromCart(item.getProductId());
            cartManager.removeFromCart(item.getProductId());
            loadCartItems(); // Refresh the cart
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!sessionManager.isGuest()) {
            loadCartItems();
        }
    }

    public void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}