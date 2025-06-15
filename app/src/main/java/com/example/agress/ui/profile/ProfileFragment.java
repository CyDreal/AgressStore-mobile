package com.example.agress.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.agress.R;
import com.example.agress.UserIdentifyActivity;
import com.example.agress.api.ApiClient;
import com.example.agress.api.response.UserResponse;
import com.example.agress.databinding.FragmentProfileBinding;
import com.example.agress.model.User;
import com.example.agress.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private View blurView;
    private static final int PICK_IMAGE_REQUEST = 1;

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
            setupViews();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup avatar click
        binding.imageAvatar.setOnClickListener(v -> showUploadAvatarDialog());
        loadAvatar();
    }

    private void loadAvatar() {
        String avatarUrl = sessionManager.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.error_img)
                    .circleCrop()
                    .into(binding.imageAvatar);
        } else {
            // Fetch user data if avatar is not in session
            ApiClient.getClient().getUser(sessionManager.getUserId())
                    .enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call,
                                               Response<UserResponse> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().getUser() != null) {
                                User user = response.body().getUser();
                                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                                    sessionManager.saveAvatar(user.getAvatar());
                                    loadAvatar(); // Reload avatar
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            Toast.makeText(requireContext(),
                                    "Failed to load avatar", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showUploadAvatarDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_avatar, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogView.findViewById(R.id.btn_choose_photo).setOnClickListener(v -> {
            dialog.dismiss();
            openImagePicker();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        try {
            String filePath = getRealPathFromUri(imageUri);
            File file = new File(filePath);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar",
                    file.getName(), requestFile);

            ApiClient.getClient().updateAvatar(sessionManager.getUserId(), body)
                    .enqueue(new Callback<UserResponse>() {
                        @Override
                        public void onResponse(Call<UserResponse> call,
                                               Response<UserResponse> response) {
                            progressDialog.dismiss();
                            if (response.isSuccessful() && response.body() != null) {
                                UserResponse userResponse = response.body();
                                if (userResponse.getStatus() == 1 && userResponse.getUser() != null) {
                                    User user = userResponse.getUser();
                                    sessionManager.saveAvatar(user.getAvatar());
                                    loadAvatar();
                                    Toast.makeText(requireContext(),
                                            "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Failed to update avatar", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(requireContext(),
                                        "Error updating avatar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResponse> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(),
                                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(requireContext(),
                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(uri, projection,
                null, null, null);
        if (cursor == null) return null;

        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        String path = cursor.getString(columnIndex);
        cursor.close();

        return path;
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

    private void setupViews() {

        binding.buttonEditProfile.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.editProfileFragment);
        });

        binding.layoutAboutUs.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profile_to_about_us);
        });

        binding.layoutAccountSettings.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_settings);
        });

        binding.layoutShippingAddress.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_profile_to_shippingAddress);
        });

        binding.layoutOrderHistory.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profile_to_order_history);
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