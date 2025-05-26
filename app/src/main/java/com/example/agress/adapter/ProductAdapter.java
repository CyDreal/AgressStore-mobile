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

    public ProductAdapter(Context context) {
        this.context = context;
    }
    public interface onProductClickListener {
        void onProductClick(Product product);
    }
    private onProductClickListener listener;
    public void setProductsClickListener(onProductClickListener listener) {
        this.listener = listener;
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

        // Set product name
        holder.productName.setText(product.getProductName());

        // Format and set price
        holder.price.setText(String.format("Rp %,d", product.getPrice()));

        // Set view count
        holder.viewCount.setText(String.valueOf(product.getViewCount()));

        // Load image with image_order = 0
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // Find image with image_order = 0
            ProductImage mainImage = product.getImages().stream()
                    .filter(img -> img.getImageOrder() == 0)
                    .findFirst()
                    .orElse(product.getImages().get(0));

            // Use image_url directly from API response
            Glide.with(context)
                    .load(mainImage.getImageUrl())
                    .placeholder(R.drawable.default_img)
                    .error(R.drawable.error_img)
                    .into(holder.productImage);
        }

        // Handle sold out status
        holder.soldOut.setVisibility(product.getStock() == 0 ? View.VISIBLE : View.GONE);

        // Set click listener for the item
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

    // Method to update the filtered product list
    public void setProducts(List<Product> newProducts) {
        this.products.clear();
        this.products.addAll(newProducts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, price, viewCount, soldOut;

        ViewHolder(View itemView) {
            super(itemView);
            // Initialize views
            productImage = itemView.findViewById(R.id.ivProduct);
            productName = itemView.findViewById(R.id.tvProductName);
            price = itemView.findViewById(R.id.tvPrice);
            viewCount = itemView.findViewById(R.id.tvViewCount);
            soldOut = itemView.findViewById(R.id.tvSoldOut);
        }
    }

}
