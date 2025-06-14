package com.example.agress.ui.profile;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;

import com.example.agress.R;
import com.example.agress.databinding.FragmentShippingAddressBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;


public class ShippingAddressFragment extends Fragment {
    private FragmentShippingAddressBinding binding;
    private BottomNavigationView bottomNav;

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentShippingAddressBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        handleBottomNavVisibility(false);
        setupRecyclerView();
        checkAddressListEmpty();

        binding.btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void showAddAddressDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_add_address, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Setup cancel button
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            // Clear all input fields before dismissing
            clearInputFields(dialogView);
            dialog.dismiss();
        });

        // Get views
//        AutoCompleteTextView provinceDropdown = dialogView.findViewById(R.id.dropdown_province);
//        AutoCompleteTextView cityDropdown = dialogView.findViewById(R.id.dropdown_city);

        // Setup province dropdown
//        setupProvinceDropdown(provinceDropdown, cityDropdown);

        // Setup buttons
//        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
//        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
//            // Validate and save address
//            if (validateForm(dialogView)) {
//                saveAddress(dialogView);
//                dialog.dismiss();
//            }
//        });

        dialog.show();
    }

    private void clearInputFields(View dialogView) {
        // Clear radio group selection
        RadioGroup labelGroup = dialogView.findViewById(R.id.radio_group_label);
        labelGroup.clearCheck();

        // Clear text input fields
        ((TextInputLayout) dialogView.findViewById(R.id.input_recipient_name)).getEditText().setText("");
        ((TextInputLayout) dialogView.findViewById(R.id.input_phone)).getEditText().setText("");
        ((TextInputLayout) dialogView.findViewById(R.id.input_address)).getEditText().setText("");
        ((TextInputLayout) dialogView.findViewById(R.id.input_postal_code)).getEditText().setText("");
        ((TextInputLayout) dialogView.findViewById(R.id.input_notes)).getEditText().setText("");

        // Clear dropdowns
        ((AutoCompleteTextView) dialogView.findViewById(R.id.dropdown_province)).setText("");
        ((AutoCompleteTextView) dialogView.findViewById(R.id.dropdown_city)).setText("");
    }

    private void setupRecyclerView() {
        binding.recyclerAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: Set your adapter here
    }

    private void checkAddressListEmpty() {
        // Assuming you have a list of addresses
        // List<Address> addressList = yourAddressList;

        // For testing, using null or empty list
        List<?> addressList = null; // or new ArrayList<>();

        if (addressList == null || addressList.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.recyclerAddresses.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyclerAddresses.setVisibility(View.VISIBLE);
        }
    }

    private void handleBottomNavVisibility(boolean show) {
        if (getActivity() != null) {
            bottomNav = getActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        handleBottomNavVisibility(true);
        super.onDestroyView();
        binding = null;
    }
}