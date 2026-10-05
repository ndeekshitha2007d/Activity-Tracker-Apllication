package com.example.activitytracker;

public class StepData {
    private final long totalSteps;
    private final double distanceKm;
    private final int caloriesBurned;

    public StepData(long totalSteps) {
        this.totalSteps = totalSteps;
        // Standard conversions based on average stride length and caloric expenditure
        this.distanceKm = (totalSteps * 0.762) / 1000.0; // ~0.762 meters per step
        this.caloriesBurned = (int) (totalSteps * 0.04);  // ~0.04 kcal per step
    }

    public long getTotalSteps() { return totalSteps; }
    public double getDistanceKm() { return distanceKm; }
    public int getCaloriesBurned() { return caloriesBurned; }
}