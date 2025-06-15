package com.example.agress.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agress.R;
import com.example.agress.databinding.ItemOrderHistoryBinding;
import com.example.agress.model.Order;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders = new ArrayList<>();

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderHistoryBinding binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderHistoryBinding binding;
        private final OrderItemAdapter itemAdapter;

        OrderViewHolder(ItemOrderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemAdapter = new OrderItemAdapter();
            binding.recyclerOrderItems.setAdapter(itemAdapter);
            binding.recyclerOrderItems.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        }

        void bind(Order order) {
            binding.textOrderId.setText("Order #" + order.getId());
            binding.textOrderDate.setText(formatDate(order.getCreatedAt()));
            binding.textStatus.setText(order.getPaymentStatus().toUpperCase());
            binding.textTotal.setText(formatPrice(order.getTotalPrice()));

            // Set status background color
            int bgColor = getStatusColor(order.getPaymentStatus());
            binding.textStatus.getBackground().setTint(bgColor);

            // Set order items
            itemAdapter.setItems(order.getItems());

            // Optional: Set click listener for details button
            binding.buttonDetails.setOnClickListener(v -> {
                // Handle details click
            });
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateStr;
            }
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

        private int getStatusColor(String status) {
            Context context = binding.getRoot().getContext();
            switch (status.toLowerCase()) {
                case "paid":
                    return context.getColor(R.color.success);
                case "unpaid":
                    return context.getColor(R.color.error);
                default:
                    return context.getColor(R.color.primary);
            }
        }
    }
}