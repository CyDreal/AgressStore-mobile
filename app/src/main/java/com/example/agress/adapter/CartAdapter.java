package com.example.agress.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agress.R;
import com.example.agress.api.ApiService;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.CartResponse;
import com.example.agress.model.CartItem;
import com.example.agress.ui.cart.CartFragment;
import com.example.agress.utils.CurrencyFormatter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final CartItemListener listener;
    private final Context context;
    private final boolean isLoggedIn;
    private final ApiService apiService;
    private final String userId;
    private final CartFragment cartFragment;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener listener,
                       Context context, boolean isLoggedIn, ApiService apiService, String userId, CartFragment cartFragment) {
        this.cartItems = cartItems;
        this.listener = listener;
        this.context = context;
        this.isLoggedIn = isLoggedIn;
        this.apiService = apiService;
        this.userId = userId;
        this.cartFragment = cartFragment;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateItems(List<CartItem> newItems) {
        cartItems.clear();
        cartItems.addAll(newItems);
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView productStock;
        private final TextView productQuantity;
        private final ImageButton btnDecrease;
        private final ImageButton btnIncrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.image_product);
            productName = itemView.findViewById(R.id.text_product_name);
            productPrice = itemView.findViewById(R.id.text_price);
            productStock = itemView.findViewById(R.id.text_stock);
            productQuantity = itemView.findViewById(R.id.text_quantity);
            btnDecrease = itemView.findViewById(R.id.button_minus);
            btnIncrease = itemView.findViewById(R.id.button_plus);
        }

        public void bind(CartItem item) {
            // Load product image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.error_img)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.error_img);
            }

            productName.setText(item.getProductName());
            productPrice.setText(CurrencyFormatter.formatRupiah(item.getPrice()));
            productQuantity.setText(String.valueOf(item.getQuantity()));
            productStock.setText(context.getString(R.string.stock_available, item.getStock()));

            // Update button states based on quantity
            updateButtonStates(item);

            // Handle quantity changes
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() == 1) {
                    showRemoveConfirmation(item);
                } else {
                    int newQuantity = item.getQuantity() - 1;
                    updateItemQuantity(item, newQuantity);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                if (item.getStock() > 0 && newQuantity <= item.getStock()) {
                    updateItemQuantity(item, newQuantity);
                } else {
                    listener.showError(context.getString(R.string.max_stock_reached));
                }
            });
        }

        private void updateButtonStates(CartItem item) {
            if (item.getQuantity() == 1) {
                // Change minus button to delete button
                btnDecrease.setImageResource(R.drawable.ic_delete_24px);
                btnDecrease.setContentDescription(context.getString(R.string.delete_item));
            } else {
                // Reset to minus button
                btnDecrease.setImageResource(R.drawable.remove_24px);
                btnDecrease.setContentDescription(context.getString(R.string.decrease_quantity));
            }
        }

        private void updateItemQuantity(CartItem item, int newQuantity) {
            if (isLoggedIn) {
                updateQuantityOnServer(item, newQuantity);
            } else {
                // Update locally
                listener.onQuantityChanged(item, newQuantity);
            }
        }

        private void updateQuantityOnServer(CartItem item, int newQuantity) {
            if (item.getProductId() == 0) {
                listener.showError("Invalid product");
                return;
            }

            listener.showLoading();
            apiService.addToCart(userId, item.getProductId(), newQuantity)
                    .enqueue(new Callback<CartResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CartResponse> call,
                                               @NonNull Response<CartResponse> response) {
                            listener.hideLoading();
                            if (response.isSuccessful() && response.body() != null) {
                                item.setQuantity(newQuantity);
                                notifyItemChanged(getAdapterPosition());
                                listener.onQuantityChanged(item, newQuantity);
                                cartFragment.updateUI();
                            } else {
                                listener.showError("Failed to update quantity");
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

        private void showRemoveConfirmation(CartItem item) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.remove_item)
                    .setMessage(R.string.confirm_remove_item)
                    .setPositiveButton(R.string.remove, (dialog, which) -> {
                        if (isLoggedIn) {
                            removeFromServer(item);
                        } else {
                            listener.onRemoveItem(item);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        private void removeFromServer(CartItem item) {
            listener.showLoading();
            apiService.removeFromCart(String.valueOf(item.getId()))
                    .enqueue(new Callback<BaseResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<BaseResponse> call,
                                               @NonNull Response<BaseResponse> response) {
                            listener.hideLoading();
                            if (response.isSuccessful()) {
                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    cartItems.remove(position);
                                    notifyItemRemoved(position);
                                    listener.onRemoveItem(item);
                                    cartFragment.updateUI();
                                }
                            } else {
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
    }
}