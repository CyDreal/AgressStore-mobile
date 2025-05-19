package com.example.agress;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.agress.auth.AuthPagerAdapter;
import com.example.agress.databinding.ActivityUserIdentifyBinding;
import com.google.android.material.tabs.TabLayout;

public class UserIdentifyActivity extends AppCompatActivity {

    private ActivityUserIdentifyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserIdentifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Ubah set ke binding getRoot

        // Set up ViewPager menggunakan AuthPagerAdapter
        ViewPager2 viewPager = binding.viewPager;
        viewPager.setAdapter(new AuthPagerAdapter(this));

        // Set up TabLayout menggunakan ViewPager
        TabLayout tabLayout = binding.tabLayout;
        TabLayout.Tab tabLogin = tabLayout.newTab().setText("Login");
        TabLayout.Tab tabRegister = tabLayout.newTab().setText("Register");

        tabLayout.addTab(tabLogin);
        tabLayout.addTab(tabRegister);

        // Mensinkronkan TabLayout dengan ViewPager
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }
    public void switchToLogin() {
        binding.viewPager.setCurrentItem(0); // 0 adalah index dari tab login
    }
}