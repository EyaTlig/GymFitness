package com.fitpro.myapplication2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .setLaunchSingleTop(true)
                .build();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();


            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == id) {
                return true;
            }

            navController.navigate(id, null, options);
            return true;
        });
    }
}