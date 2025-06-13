package com.example.agress.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agress.R;
import com.example.agress.api.ApiClient;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.CartListResponse;
import com.example.agress.api.response.CartResponse;
import com.example.agress.databinding.ItemCartBinding;
import com.example.agress.model.Cart;
import com.example.agress.model.CartItem;
import com.example.agress.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItems;
    private final CartItemListener listener;
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    public CartAdapter(CartItemListener listener, Context context) {
        this.cartItems = new ArrayList<>();
        this.listener = listener;
        this.apiService = ApiClient.getClient();
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;
        private CartItem currentItem;

        ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            this.currentItem = item; // Simpan reference ke item saat ini
            updateUI(item);
            setupListeners(item);
        }

        private void updateUI(CartItem item) {
            binding.textProductName.setText(item.getProductName());
            binding.textPrice.setText(String.format("Rp %,d", item.getPrice()));
            binding.textQuantity.setText(String.valueOf(item.getQuantity()));
            binding.textStock.setText(String.format("Stok: %d", item.getStock()));

            Glide.with(binding.getRoot())
                    .load(item.getImageUrl())
                    .into(binding.imageProduct);

            updateMinusButton(item);
        }

        private void setupListeners(CartItem item) {
            binding.buttonMinus.setOnClickListener(v -> {
                if (item.getQuantity() == 1) {
                    handleRemoveItem(item);
                } else {
                    handleQuantityDecrease(item);
                }
            });

            binding.buttonPlus.setOnClickListener(v -> {
                if (item.getQuantity() < item.getStock()) {
                    handleQuantityIncrease(item);
                } else {
                    listener.showError("Maximum stock reached");
                }
            });
        }

        private void handleQuantityDecrease(CartItem item) {
            listener.showLoading();
            updateCartInDatabase(item, item.getQuantity() - 1);
        }

        private void handleQuantityIncrease(CartItem item) {
            listener.showLoading();
            updateCartInDatabase(item, item.getQuantity() + 1);
        }

        private void handleRemoveItem(CartItem item) {
            Log.d("CartAdapter", "Removing cart item with ID: " + item.getId());
            if (item.getId() == 0) {
                // Refresh cart data from API first
                refreshCartData();
                listener.showError("Invalid cart ID");
                return;
            }

            listener.showLoading();
            // Gunakan cart ID langsung
            apiService.removeFromCart(String.valueOf(item.getId()))
                    .enqueue(new Callback<BaseResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<BaseResponse> call,
                                               @NonNull Response<BaseResponse> response) {
                            if (response.isSuccessful()) {
                                refreshCartData();
                                listener.onRemoveItem(item);
                            } else {
                                listener.hideLoading();
                                listener.showError("Failed to remove item");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<BaseResponse> call,
                                              @NonNull Throwable t) {
                            listener.hideLoading();
                            listener.showError("Network error");
                        }
                    });
        }

        private void updateCartInDatabase(CartItem item, int newQuantity) {
            apiService.addToCart(
                    sessionManager.getUserId(),
                    item.getProductId(),
                    newQuantity
            ).enqueue(new Callback<CartResponse>() {
                @Override
                public void onResponse(@NonNull Call<CartResponse> call,
                                       @NonNull Response<CartResponse> response) {
                    if (response.isSuccessful()) {
                        refreshCartData();
                    } else {
                        listener.hideLoading();
                        listener.showError("Failed to update cart");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CartResponse> call,
                                      @NonNull Throwable t) {
                    listener.hideLoading();
                    listener.showError("Network error");
                }
            });
        }

        private void refreshCartData() {
            apiService.getUserCarts(sessionManager.getUserId())
                    .enqueue(new Callback<CartListResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CartListResponse> call,
                                               @NonNull Response<CartListResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                updateSessionManagerCart(response.body().getCarts());
                            } else {
                                listener.showError("Failed to refresh cart");
                            }
                            listener.hideLoading();
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartListResponse> call,
                                              @NonNull Throwable t) {
                            listener.hideLoading();
                            listener.showError("Network error");
                        }
                    });
        }

        private void updateSessionManagerCart(List<Cart> carts) {
            sessionManager.clearCart();
            for (Cart cart : carts) {
                CartItem item = cart.toCartItem();
                if (item != null) {
                    sessionManager.addToCart(item);
                    // Update UI jika item ini adalah item yang sedang ditampilkan
                    if (item.getProductId() == currentItem.getProductId()) {
                        binding.textQuantity.setText(String.valueOf(item.getQuantity()));
                        updateMinusButton(item);
                    }
                }
            }
            listener.onQuantityChanged(currentItem, currentItem.getQuantity());
        }

        private void updateMinusButton(CartItem item) {
            if (item.getQuantity() == 1) {
                binding.buttonMinus.setImageResource(R.drawable.ic_delete_24px);
                binding.buttonMinus.setColorFilter(
                        ContextCompat.getColor(binding.getRoot().getContext(),
                                android.R.color.holo_red_light)
                );
            } else {
                binding.buttonMinus.setImageResource(R.drawable.remove_24px);
                binding.buttonMinus.setColorFilter(
                        ContextCompat.getColor(binding.getRoot().getContext(),
                                android.R.color.darker_gray)
                );
            }
        }
    }
}