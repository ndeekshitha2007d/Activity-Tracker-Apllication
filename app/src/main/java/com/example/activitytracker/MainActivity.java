package com.example.activitytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.health.connect.client.PermissionController;
import com.example.activitytracker.databinding.ActivityMainBinding;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FitnessTrackerPrefs";
    private static final String KEY_CACHED_STEPS = "cached_steps";

    private ActivityMainBinding binding;
    private HealthConnectManager healthConnectManager;
    private ActivityResultLauncher<Set<String>> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadCachedData();

        healthConnectManager = new HealthConnectManager(this);

        requestPermissionLauncher = registerForActivityResult(
                PermissionController.createRequestPermissionResultContract(),
                grantedPermissions -> {
                    if (grantedPermissions != null && grantedPermissions.containsAll(HealthConnectManager.PERMISSIONS)) {
                        onPermissionsGranted();
                    } else {
                        Toast.makeText(this, "Permission denied. Steps cannot be retrieved.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        binding.btnRefresh.setOnClickListener(v -> checkAndRequestPermissions());

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (!HealthConnectManager.isHealthConnectAvailable(this)) {
            Toast.makeText(this, "Health Connect is not available.", Toast.LENGTH_LONG).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Set<String> granted = healthConnectManager.getGrantedPermissions();
            runOnUiThread(() -> {
                if (granted.containsAll(HealthConnectManager.PERMISSIONS)) {
                    onPermissionsGranted();
                } else {
                    requestPermissionLauncher.launch(HealthConnectManager.PERMISSIONS);
                }
            });
        });
    }

    private void onPermissionsGranted() {
        loadFitnessData();
    }

    private void loadFitnessData() {
        healthConnectManager.fetchTodaySteps(new HealthConnectManager.HealthDataCallback() {
            @Override
            public void onSuccess(StepData stepData) {
                runOnUiThread(() -> updateUI(stepData));
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Failed to load steps: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void updateUI(StepData data) {
        long steps = data.getTotalSteps();
        saveStepsToCache(steps);

        binding.tvStepCount.setText(String.format(Locale.getDefault(), "%,d", steps));
        binding.stepsProgressBar.setProgress((int) Math.min(steps, 10000), true);
        binding.tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", data.getDistanceKm()));
        binding.tvCalories.setText(String.format(Locale.getDefault(), "%d kcal", data.getCaloriesBurned()));
    }

    private void saveStepsToCache(long steps) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_CACHED_STEPS, steps).apply();
    }

    private void loadCachedData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long cachedSteps = prefs.getLong(KEY_CACHED_STEPS, 0);
        if (cachedSteps > 0) {
            updateUI(new StepData(cachedSteps));
        }
    }
}