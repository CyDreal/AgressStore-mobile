package com.example.agress.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agress.R;
import com.example.agress.databinding.ItemCartBinding;
import com.example.agress.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItems;
    private final CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(CartItemListener listener) {
        this.cartItems = new ArrayList<>();
        this.listener = listener;
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

        ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            binding.textProductName.setText(item.getProductName());
            binding.textPrice.setText(String.format("Rp %,d", item.getPrice()));
            binding.textQuantity.setText(String.valueOf(item.getQuantity()));
            binding.textStock.setText(String.format("Stok: %d", item.getStock()));

            Glide.with(binding.getRoot())
                .load(item.getImageUrl())
                .into(binding.imageProduct);

            // Update minus button appearance and behavior based on quantity
            updateMinusButton(item);

            // Setup quantity controls dengan update langsung
            binding.buttonMinus.setOnClickListener(v -> {
                if (item.getQuantity() == 1) {
                    // Remove item if quantity is 1
                    listener.onRemoveItem(item);
                } else {
                    // Decrease quantity
                    int newQuantity = item.getQuantity() - 1;
                    item.setQuantity(newQuantity);
                    binding.textQuantity.setText(String.valueOf(newQuantity));
                    updateMinusButton(item);
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            binding.buttonPlus.setOnClickListener(v -> {
                if (item.getQuantity() < item.getStock()) {
                    int newQuantity = item.getQuantity() + 1;
                    item.setQuantity(newQuantity);
                    binding.textQuantity.setText(String.valueOf(newQuantity));
                    listener.onQuantityChanged(item, newQuantity);
                }
            });
        }

        private void updateMinusButton(CartItem item) {
            if (item.getQuantity() == 1) {
                binding.buttonMinus.setImageResource(R.drawable.ic_delete_24px);
                binding.buttonMinus.setColorFilter(
                        ContextCompat.getColor(binding.getRoot().getContext(), android.R.color.holo_red_light)
                );
            } else {
                binding.buttonMinus.setImageResource(R.drawable.remove_24px);
                binding.buttonMinus.setColorFilter(
                        ContextCompat.getColor(binding.getRoot().getContext(), android.R.color.darker_gray)
                );
            }
        }
    }
}