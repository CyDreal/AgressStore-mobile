package com.example.agress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agress.R;
import com.example.agress.model.ShippingService;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
    private List<ShippingService> services = new ArrayList<>();
    private OnServiceSelectedListener listener;

    public interface OnServiceSelectedListener {
        void onServiceSelected(ShippingService service);
    }

    public void setServices(List<ShippingService> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    public void setListener(OnServiceSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipping_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(services.get(position));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textService, textDescription, textEstimate, textCost;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textService = itemView.findViewById(R.id.textService);
            textDescription = itemView.findViewById(R.id.textDescription);
            textEstimate = itemView.findViewById(R.id.textEstimate);
            textCost = itemView.findViewById(R.id.textCost);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onServiceSelected(services.get(position));
                }
            });
        }

        void bind(ShippingService service) {
            textService.setText(service.getService());
            textDescription.setText(service.getDescription());
            textEstimate.setText(String.format("Estimasi: %s hari", service.getCost().getEtd()));
            textCost.setText(String.format("Rp %,d", service.getCost().getValue()));
        }
    }
}
