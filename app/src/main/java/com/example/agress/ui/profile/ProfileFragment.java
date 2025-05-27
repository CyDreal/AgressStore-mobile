package com.example.agress.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.agress.R;
import com.example.agress.UserIdentifyActivity;
import com.example.agress.databinding.FragmentProfileBinding;
import com.example.agress.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private View blurView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        sessionManager = new SessionManager(requireContext());

        if (sessionManager.isGuest()) {
            showLoginRequiredDialog();
        } else {
            setupUserInfo();
            setupLogoutButton();
        }

        return binding.getRoot();
    }

    private void showLoginRequiredDialog() {
        // Create blur view
        blurView = new View(requireContext());
        blurView.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        ((ViewGroup) requireActivity().getWindow().getDecorView().getRootView()).addView(blurView, params);

        // Inflate custom dialog
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_login_required, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Set transparent background for rounded corners
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Button click handlers
        dialogView.findViewById(R.id.btnLogin).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(requireContext(), UserIdentifyActivity.class);
            startActivity(intent);
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).navigateUp();
        });

        // Dialog dismiss listener
        dialog.setOnDismissListener(dialogInterface -> {
            // Remove blur view when dialog is dismissed
            ((ViewGroup) requireActivity().getWindow().getDecorView().getRootView()).removeView(blurView);
        });

        dialog.show();
    }

    private void setupUserInfo() {
        binding.textName.setText(sessionManager.getUsername());
        binding.textEmail.setText(sessionManager.getEmail());
    }

    private void setupLogoutButton() {
        binding.buttonLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear session
                        sessionManager.logout();

                        // Navigate to login screen
                        Intent intent = new Intent(requireContext(), UserIdentifyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (blurView != null && blurView.getParent() != null) {
            ((ViewGroup) blurView.getParent()).removeView(blurView);
        }
        binding = null;
    }
}