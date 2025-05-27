package com.example.agress.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agress.R;
import com.example.agress.model.Product;
import com.example.agress.model.ProductImage;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final Context context;
    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public ProductAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        // Load product image
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String imageUrl = product.getImages().get(0).getImageUrl();
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.error_img)
                    .into(holder.ivProduct);
        }

        // Set product details
        holder.tvProductName.setText(product.getProductName());
        holder.tvPrice.setText(String.format("Rp %,d", product.getPrice()));
        holder.tvViewCount.setText(String.valueOf(product.getViewCount()));
        // Add purchased quantity
        holder.tvPurchased.setText(String.valueOf(product.getPurchaseQuantity()));

        // Handle product availability
        boolean isAvailable = "available".equals(product.getStatus());
        holder.tvSoldOut.setVisibility(isAvailable ? View.GONE : View.VISIBLE);

        // Handle click event
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setProductsClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName;
        TextView tvPrice;
        TextView tvViewCount, tvPurchased;
        TextView tvSoldOut;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvViewCount = itemView.findViewById(R.id.tvViewCount);
            tvSoldOut = itemView.findViewById(R.id.tvSoldOut);
            tvPurchased = itemView.findViewById(R.id.tvPurchased);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}