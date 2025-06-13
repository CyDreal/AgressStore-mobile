package com.example.agress.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.example.agress.R;
import com.example.agress.databinding.FragmentAboutUsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AboutUsFragment extends Fragment {
    private WebView mapWebView;
    private FragmentAboutUsBinding binding;
    private BottomNavigationView bottomNav;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAboutUsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMapWebView();
        setupToolbar();
        handleBottomNavVisibility(false);
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void handleBottomNavVisibility(boolean show) {
        if (getActivity() != null) {
            bottomNav = getActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void setupMapWebView() {
        mapWebView = binding.mapWebView;
        mapWebView.getSettings().setJavaScriptEnabled(true);

        // HTML content with iframe
        String htmlContent = "<html><body>" +
                "<iframe src=\"https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3960.0131412595706!2d110.4591828744672!3d-7.007734968639391!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x2e708d9ab90b4b8f%3A0xec2ca2a4d252ebf5!2sAGRES%20ID%20SEMARANG!5e0!3m2!1sid!2sid!4v1749808862366!5m2!1sid!2sid\" " +
                "width=\"100%\" height=\"100%\" style=\"border:0;\" " +
                "allowfullscreen=\"\" loading=\"lazy\" " +
                "referrerpolicy=\"no-referrer-when-downgrade\"></iframe>" +
                "</body></html>";

        // Load HTML content
        mapWebView.loadData(htmlContent, "text/html", "utf-8");
    }

    @Override
    public void onDestroyView() {
        handleBottomNavVisibility(true);
        super.onDestroyView();
        binding = null;
    }
}