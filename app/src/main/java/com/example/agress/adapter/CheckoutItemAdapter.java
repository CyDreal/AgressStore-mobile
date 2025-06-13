package com.example.agress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agress.R;
import com.example.agress.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {
    private List<CartItem> items = new ArrayList<>();

    public void setItems(List<CartItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.textProductName.setText(item.getProductName());
        holder.textPrice.setText(String.format("Rp %,d", item.getPrice()));
        holder.textQuantity.setText(String.format("x%d", item.getQuantity()));

        // Load image using Glide
        Glide.with(holder.imageProduct.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.remove_24px) // fix it then
                .into(holder.imageProduct);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textPrice, textQuantity;

        ViewHolder(View view) {
            super(view);
            imageProduct = view.findViewById(R.id.imageProduct);
            textProductName = view.findViewById(R.id.textProductName);
            textPrice = view.findViewById(R.id.textPrice);
            textQuantity = view.findViewById(R.id.textQuantity);
        }
    }
}
