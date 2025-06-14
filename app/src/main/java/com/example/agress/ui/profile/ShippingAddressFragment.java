package com.example.agress.ui.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agress.R;
import com.example.agress.adapter.AddressAdapter;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.AddressResponse;
import com.example.agress.api.response.BaseResponse;
import com.example.agress.api.response.RajaOngkirResponse;
import com.example.agress.databinding.FragmentShippingAddressBinding;
import com.example.agress.model.Address;
import com.example.agress.model.City;
import com.example.agress.model.Province;
import com.example.agress.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ShippingAddressFragment extends Fragment {
    private FragmentShippingAddressBinding binding;
    private SessionManager sessionManager;
    private BottomNavigationView bottomNav;
    private ArrayAdapter<Province> provinceAdapter;
    private ArrayAdapter<City> cityAdapter;
    private List<Province> provinces = new ArrayList<>();
    private List<City> cities = new ArrayList<>();
    private Province selectedProvince;
    private City selectedCity;
    private AddressAdapter addressAdapter;

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = FragmentShippingAddressBinding.inflate(inflater, container, false);
       sessionManager = new SessionManager(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        handleBottomNavVisibility(false);
        setupRecyclerView();
        loadAddresses();
        checkAddressListEmpty();

        binding.btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void setupAddressDialog(View dialogView) {
        AutoCompleteTextView provinceDropdown = dialogView.findViewById(R.id.dropdown_province);
        AutoCompleteTextView cityDropdown = dialogView.findViewById(R.id.dropdown_city);

        // Setup province adapter
        provinceAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, provinces);
        provinceDropdown.setAdapter(provinceAdapter);

        // Setup city adapter
        cityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, cities);
        cityDropdown.setAdapter(cityAdapter);

        // Province selection listener
        provinceDropdown.setOnItemClickListener((parent, view, position, id) -> {
            Province newSelectedProvince = provinces.get(position);

            // Reset city data when province changes
            cities.clear();
            cityAdapter.notifyDataSetChanged();
//            cityDropdown.setText("");
            selectedCity = null;
//            TextInputLayout postalCodeInput = dialogView.findViewById(R.id.input_postal_code);
//            postalCodeInput.getEditText().setText("");

            // Request cities data when province is selected
            ApiClient.getClient().getCities(newSelectedProvince.getProvinceId())
                    .enqueue(new Callback<RajaOngkirResponse<City>>() {
                        @Override
                        public void onResponse(Call<RajaOngkirResponse<City>> call,
                                               Response<RajaOngkirResponse<City>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                requireActivity().runOnUiThread(() -> {
                                    // Reset city data
                                    cities.clear();
                                    cities.addAll(response.body().getRajaongkir().getResults());
                                    cityAdapter.notifyDataSetChanged();
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<RajaOngkirResponse<City>> call, Throwable t) {
                            Toast.makeText(requireContext(),
                                    "Failed to load cities: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            selectedProvince = newSelectedProvince;
        });

        // City selection listener
        cityDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = cities.get(position);
            if (selectedCity != null) {
                TextInputLayout postalCodeInput = dialogView.findViewById(R.id.input_postal_code);
                postalCodeInput.getEditText().setText(selectedCity.getPostalCode());
            }
        });
    }

    private void showAddAddressDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_add_address, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Create progress dialog
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Saving address...");
        progressDialog.setCancelable(false);

        setupAddressDialog(dialogView);

        // Request provinces data immediately when dialog shows
        ApiClient.getClient().getProvinces().enqueue(new Callback<RajaOngkirResponse<Province>>() {
            @Override
            public void onResponse(Call<RajaOngkirResponse<Province>> call,
                                   Response<RajaOngkirResponse<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requireActivity().runOnUiThread(() -> {
                        provinces.clear();
                        provinces.addAll(response.body().getRajaongkir().getResults());
                        provinceAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(Call<RajaOngkirResponse<Province>> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Failed to load provinces: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Setup buttons
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (validateForm(dialogView)) {
                progressDialog.show();
                saveAddress(dialogView, progressDialog, dialog);
            }
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            clearInputFields(dialogView);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadAddresses() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getClient().getAddresses(sessionManager.getUserId())
                .enqueue(new Callback<AddressResponse>() {
                    @Override
                    public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Address> addresses = response.body().getAddresses();
                            if (addresses.isEmpty()) {
                                binding.layoutEmpty.setVisibility(View.VISIBLE);
                                binding.recyclerAddresses.setVisibility(View.GONE);
                            } else {
                                binding.layoutEmpty.setVisibility(View.GONE);
                                binding.recyclerAddresses.setVisibility(View.VISIBLE);
                                addressAdapter.setAddresses(addresses);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AddressResponse> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(),
                                "Failed to load addresses: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateForm(View dialogView) {
        boolean isValid = true;

        // Validate label
        RadioGroup labelGroup = dialogView.findViewById(R.id.radio_group_label);
        if (labelGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select an address label", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate recipient name
        String recipientName = getInputText(dialogView, R.id.input_recipient_name);
        if (recipientName.trim().isEmpty()) {
            ((TextInputLayout) dialogView.findViewById(R.id.input_recipient_name))
                    .setError("Recipient name is required");
            isValid = false;
        }

        // Validate phone
        String phone = getInputText(dialogView, R.id.input_phone);
        if (phone.trim().isEmpty()) {
            ((TextInputLayout) dialogView.findViewById(R.id.input_phone))
                    .setError("Phone number is required");
            isValid = false;
        }

        // Validate province
        if (selectedProvince == null) {
            ((TextInputLayout) dialogView.findViewById(R.id.input_province))
                    .setError("Please select a province");
            isValid = false;
        }

        // Validate city
        if (selectedCity == null) {
            ((TextInputLayout) dialogView.findViewById(R.id.input_city))
                    .setError("Please select a city");
            isValid = false;
        }

        // Validate address
        String address = getInputText(dialogView, R.id.input_address);
        if (address.trim().isEmpty()) {
            ((TextInputLayout) dialogView.findViewById(R.id.input_address))
                    .setError("Address is required");
            isValid = false;
        }

        // Validate postal code
        String postalCode = getInputText(dialogView, R.id.input_postal_code);
        if (postalCode.trim().isEmpty()) {
            ((TextInputLayout) dialogView.findViewById(R.id.input_postal_code))
                    .setError("Postal code is required");
            isValid = false;
        }

        return isValid;
    }

    private void saveAddress(View dialogView, ProgressDialog progressDialog, AlertDialog dialog) {
        // Get all form data
        String label = getSelectedLabel(dialogView);
        String recipientName = getInputText(dialogView, R.id.input_recipient_name);
        String phone = getInputText(dialogView, R.id.input_phone);
        String fullAddress = getInputText(dialogView, R.id.input_address);
        String postalCode = getInputText(dialogView, R.id.input_postal_code);
        String notes = getInputText(dialogView, R.id.input_notes);

        // Get IDs from selected province and city
        String provinceId = selectedProvince != null ? selectedProvince.getProvinceId() : "";
        String provinceName = selectedProvince != null ? selectedProvince.getProvinceName() : "";
        String cityId = selectedCity != null ? selectedCity.getCityId() : "";
        String cityName = selectedCity != null ? selectedCity.getCityName() : "";

        // Create request to save address
        ApiClient.getClient().saveAddress(
                sessionManager.getUserId(),
                label,
                recipientName,
                phone,
                provinceId,
                provinceName,
                cityId,
                cityName,
                fullAddress,
                postalCode,
                notes
        ).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(),
                                "Address saved successfully",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadAddresses();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(),
                                "Failed to save address",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(),
                            "Error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getSelectedLabel(View dialogView) {
        RadioGroup labelGroup = dialogView.findViewById(R.id.radio_group_label);
        int selectedId = labelGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_home) return "Home";
        if (selectedId == R.id.radio_office) return "Office";
        if (selectedId == R.id.radio_other) return "Other";
        return "";
    }

    private String getInputText(View dialogView, int inputLayoutId) {
        TextInputLayout inputLayout = dialogView.findViewById(inputLayoutId);
        return inputLayout.getEditText() != null ? inputLayout.getEditText().getText().toString() : "";
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

        // Reset selected values
        selectedProvince = null;
        selectedCity = null;
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter(requireContext());
        binding.recyclerAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAddresses.setAdapter(addressAdapter);
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