package com.example.agress.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agress.R;
import com.example.agress.model.Address;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    private List<Address> addresses;
    private Context context;

    public AddressAdapter(Context context) {
        this.context = context;
        this.addresses = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shipping_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addresses.get(position);

        holder.labelText.setText(address.getLabel());
        holder.recipientNameText.setText(address.getRecipientName());
        holder.phoneText.setText(address.getPhone());
        holder.locationText.setText(String.format("%s, %s",
                address.getProvinceName(), address.getCityName()));
        holder.fullAddressText.setText(address.getFullAddress());
        holder.postalCodeText.setText(address.getPostalCode());
        holder.notesText.setText(address.getNotes());

        holder.primaryChip.setVisibility(address.isPrimary() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelText, recipientNameText, phoneText, locationText,
                fullAddressText, postalCodeText, notesText;
        Chip primaryChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelText = itemView.findViewById(R.id.text_label);
            recipientNameText = itemView.findViewById(R.id.text_recipient_name);
            phoneText = itemView.findViewById(R.id.text_phone);
            locationText = itemView.findViewById(R.id.text_location);
            fullAddressText = itemView.findViewById(R.id.text_full_address);
            postalCodeText = itemView.findViewById(R.id.text_postal_code);
            notesText = itemView.findViewById(R.id.text_notes);
            primaryChip = itemView.findViewById(R.id.chip_primary);
        }
    }
}