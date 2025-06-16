package com.example.agress.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agress.R;
import com.example.agress.databinding.ItemOrderHistoryBinding;
import com.example.agress.model.Order;
import com.example.agress.ui.profile.OrderHistoryFragment;

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
            binding.textStatus.setText(order.getPaymentStatus());
            binding.textStatus.setBackgroundTintList(ColorStateList.valueOf(getStatusColor(order.getPaymentStatus())));
            binding.textTotal.setText(formatPrice(order.getTotalPrice()));

            // Set items to nested RecyclerView
            itemAdapter.setItems(order.getItems());

            // Set click listener for details button here
            binding.buttonDetails.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("order_id", order.getId());
                Navigation.findNavController(v).navigate(R.id.orderDetailsFragment, args);
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