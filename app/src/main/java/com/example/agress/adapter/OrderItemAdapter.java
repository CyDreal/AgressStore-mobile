package com.example.agress.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agress.databinding.ItemOrderHistoryProductBinding;
import com.example.agress.model.OrderItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<OrderItem> items = new ArrayList<>();

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderHistoryProductBinding binding = ItemOrderHistoryProductBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderHistoryProductBinding binding;

        OrderItemViewHolder(ItemOrderHistoryProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OrderItem item) {
            binding.textProductName.setText(item.getProduct().getProductName());
            binding.textProductPrice.setText(formatPrice(item.getPrice()));
            binding.textQuantity.setText("x" + item.getQuantity());
        }

        private String formatPrice(String price) {
            try {
                double amount = Double.parseDouble(price);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                return formatter.format(amount);
            } catch (NumberFormatException e) {
                return price;
            }
        }
    }
}