package com.example.agress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agress.R;
import com.example.agress.model.Courier;

import java.util.ArrayList;
import java.util.List;

public class CourierAdapter extends RecyclerView.Adapter<CourierAdapter.ViewHolder> {
    private List<Courier> couriers = new ArrayList<>();
    private OnCourierSelectedListener listener;

    public interface OnCourierSelectedListener {
        void onCourierSelected(Courier courier);
    }

    public void setCouriers(List<Courier> couriers) {
        this.couriers = couriers;
        notifyDataSetChanged();
    }

    public void setListener(OnCourierSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_courier, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(couriers.get(position));
    }

    @Override
    public int getItemCount() {
        return couriers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView courierName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            courierName = itemView.findViewById(R.id.textCourierName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCourierSelected(couriers.get(position));
                }
            });
        }

        void bind(Courier courier) {
            courierName.setText(courier.getName());
        }
    }
}