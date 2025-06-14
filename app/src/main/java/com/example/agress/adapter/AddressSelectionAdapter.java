package com.example.agress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agress.model.Address;
import com.example.agress.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AddressSelectionAdapter extends RecyclerView.Adapter<AddressSelectionAdapter.ViewHolder> {
    private List<Address> addresses = new ArrayList<>();
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
        notifyDataSetChanged();
    }

    public void setListener(OnAddressSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.bind(address);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelText, recipientText, phoneText, fullAddressText,
                locationText, notesText;
        Chip chipPrimary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelText = itemView.findViewById(R.id.textLabel);
            recipientText = itemView.findViewById(R.id.textRecipientName);
            phoneText = itemView.findViewById(R.id.textPhone);
            fullAddressText = itemView.findViewById(R.id.textFullAddress);
            locationText = itemView.findViewById(R.id.textLocation);
            notesText = itemView.findViewById(R.id.textNotes);
            chipPrimary = itemView.findViewById(R.id.chipPrimary);
        }

        void bind(Address address) {
            labelText.setText(address.getLabel());
            recipientText.setText(address.getRecipientName());
            phoneText.setText(address.getPhone());
            fullAddressText.setText(address.getFullAddress());
            locationText.setText(String.format("%s, %s %s",
                    address.getCityName(),
                    address.getProvinceName(),
                    address.getPostalCode()));

            if (address.getNotes() != null && !address.getNotes().isEmpty()) {
                notesText.setVisibility(View.VISIBLE);
                notesText.setText("Catatan: " + address.getNotes());
            } else {
                notesText.setVisibility(View.GONE);
            }

            chipPrimary.setVisibility(address.isPrimary() ?
                    View.VISIBLE : View.GONE);
        }
    }
}
