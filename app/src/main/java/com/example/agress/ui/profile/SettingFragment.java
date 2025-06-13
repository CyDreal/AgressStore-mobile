package com.example.agress.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.agress.R;
import com.example.agress.databinding.FragmentSettingBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private BottomNavigationView bottomNav;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get reference to bottom navigation
        bottomNav = requireActivity().findViewById(R.id.nav_view);

        // Hide bottom navigation when entering settings
        hideBottomNavigation();

        // Setup back button
        setupBackButton();
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            // Navigate back to profile
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void hideBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void showBottomNavigation() {
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show bottom navigation when leaving settings
        showBottomNavigation();
        binding = null;
    }
}