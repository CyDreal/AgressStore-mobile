package com.example.agress.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.agress.databinding.FragmentCheckoutBinding;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CheckoutViewModel checkoutViewModel =
                new ViewModelProvider(this).get(CheckoutViewModel.class);

        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCheckout;
        checkoutViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}