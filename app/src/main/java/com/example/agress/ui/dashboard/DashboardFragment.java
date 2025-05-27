package com.example.agress.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.agress.R;
import com.example.agress.adapter.CategoryAdapter;
import com.example.agress.databinding.FragmentDashboardBinding;
import com.example.agress.model.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageSlider imageSlider;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        // Initialising Swipe Refresh
        swipeRefreshLayout = binding.swipeRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh the content

            swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
        });

        setupSearchButton();
        setupImageSlider();
        setupCategories();

        return binding.getRoot();
    }

    private void setupSearchButton() {
        binding.searchContainer.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboard_to_search)
        );
    }

    private void setupImageSlider() {
        imageSlider = binding.imageSliderBanner;
        ArrayList<SlideModel> slideModels = new ArrayList<>();

        // menggunakan hardcoded image langsung diambil dari drawable
        slideModels.add(new SlideModel(R.drawable.banner1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner4, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.banner5, ScaleTypes.FIT));

        // Menggunakan imageSlider yang sudah diinisialisasi
        binding.imageSliderBanner.setImageList(slideModels, ScaleTypes.FIT);
    }

    private void setupCategories() {
        List<Category> categories = Arrays.asList(
                new Category(R.drawable.ic_laptop, "Laptop"),
                new Category(R.drawable.ic_phone, "Phone"),
                new Category(R.drawable.ic_console, "Console")
        );

        CategoryAdapter adapter = new CategoryAdapter(categories);
        RecyclerView recyclerView = binding.categoryRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Category category = categories.get(position);
            // Navigate to ProductFragment with category
            Bundle bundle = new Bundle();
            bundle.putString("category", category.getName());
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_product, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}