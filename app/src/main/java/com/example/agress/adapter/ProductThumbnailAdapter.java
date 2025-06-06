package com.example.agress.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.agress.model.ProductImage;

import java.util.ArrayList;
import java.util.List;

public class ProductThumbnailAdapter extends RecyclerView.Adapter<ProductThumbnailAdapter.ThumbnailViewHolder> {
    private List<ProductImage> images = new ArrayList<>();
    private Context context;
    private ViewPager2 viewPager;
    private  int selectedPosition = 0;

    public ProductThumbnailAdapter(Context context, ViewPager2 viewPager) {
        this.context = context;
        this.viewPager = viewPager;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        int size = (int) (70 * context.getResources().getDisplayMetrics().density);
        imageView.setLayoutParams(new ViewGroup.MarginLayoutParams(size, size));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ((ViewGroup.MarginLayoutParams) imageView.getLayoutParams()).setMargins(0,0,0,0);
        return new ThumbnailViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        ProductImage image = images.get(position);
        Glide.with(context)
                .load(image.getImageUrl())
                .into(holder.imageView);

        holder.imageView.setAlpha(position == selectedPosition ? 1f : 0.5f);
        holder.imageView.setOnClickListener(v -> {
            viewPager.setCurrentItem(position);
            updateSelection(position);
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public void updateSelection(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(position);
    }

    static class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ThumbnailViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }
}
