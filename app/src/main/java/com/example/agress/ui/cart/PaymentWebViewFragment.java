package com.example.agress.ui.cart;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.agress.R;
import com.example.agress.databinding.FragmentPaymentWebViewBinding;

public class PaymentWebViewFragment extends Fragment {
    private FragmentPaymentWebViewBinding binding;
    private String paymentUrl;
    private String transactionId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPaymentWebViewBinding.inflate(inflater, container, false);

        // Get and log arguments
        if (getArguments() != null) {
            paymentUrl = getArguments().getString("payment_url");
            transactionId = getArguments().getString("transaction_id");

            Log.d("PaymentWebView", "Payment URL: " + paymentUrl);
            Log.d("PaymentWebView", "Transaction ID: " + transactionId);

            // Show transaction ID
            binding.textTransactionId.setText("Transaction ID: " + transactionId);
        }

        // Load URL in WebView with error handling
        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            binding.webView.getSettings().setJavaScriptEnabled(true);
            binding.webView.loadUrl(paymentUrl);
        } else {
            Log.e("PaymentWebView", "Payment URL is null or empty");
            Toast.makeText(requireContext(), "Invalid payment URL", Toast.LENGTH_SHORT).show();
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}