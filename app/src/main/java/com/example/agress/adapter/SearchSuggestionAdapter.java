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

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {
    private final Context context;
    private List<Product> suggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Product product);
    }

    public SearchSuggestionAdapter(Context context) {
        this.context = context;
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
            R.layout.item_search_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = suggestions.get(position);
        holder.productName.setText(product.getProductName());
        holder.price.setText(String.format("Rp %,d", product.getPrice()));

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ProductImage mainImage = product.getImages().stream()
                .filter(img -> img.getImageOrder() == 0)
                .findFirst()
                .orElse(product.getImages().get(0));

            Glide.with(context)
                .load(mainImage.getImageUrl())
                .placeholder(R.drawable.default_image)
                .error(R.drawable.error_img)
                .into(holder.thumbnail);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void setSuggestions(List<Product> suggestions) {
        this.suggestions = suggestions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView productName, price;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.ivProductThumbnail);
            productName = itemView.findViewById(R.id.tvProductName);
            price = itemView.findViewById(R.id.tvPrice);
        }
    }
}
