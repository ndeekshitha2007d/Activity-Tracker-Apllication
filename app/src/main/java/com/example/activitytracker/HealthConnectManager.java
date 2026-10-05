package com.example.activitytracker;

import android.content.Context;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.PermissionController;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.JvmClassMappingKt;
import kotlinx.coroutines.BuildersKt;

public class HealthConnectManager {

    private final HealthConnectClient healthConnectClient;

    public static final Set<String> PERMISSIONS = Collections.singleton(
            HealthPermission.getReadPermission(
                    JvmClassMappingKt.getKotlinClass(StepsRecord.class)
            )
    );

    public HealthConnectManager(Context context) {
        if (isHealthConnectAvailable(context)) {
            this.healthConnectClient = HealthConnectClient.getOrCreate(context);
        } else {
            this.healthConnectClient = null;
        }
    }

    public static boolean isHealthConnectAvailable(Context context) {
        int status = HealthConnectClient.getSdkStatus(context);
        return status == HealthConnectClient.SDK_AVAILABLE;
    }

    public HealthConnectClient getClient() {
        return healthConnectClient;
    }

    public Set<String> getGrantedPermissions() {
        if (healthConnectClient == null) {
            return Collections.emptySet();
        }
        PermissionController permissionController = healthConnectClient.getPermissionController();
        try {
            return BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> permissionController.getGrantedPermissions(continuation)
            );
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public interface HealthDataCallback {
        void onSuccess(StepData stepData);
        void onError(Exception e);
    }

    public void fetchTodaySteps(HealthDataCallback callback) {
        if (healthConnectClient == null) {
            callback.onError(new Exception("Health Connect Client not initialized"));
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Instant startTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
                        .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant endTime = Instant.now();

                AggregateRequest request = new AggregateRequest(
                        Collections.singleton(StepsRecord.COUNT_TOTAL),
                        TimeRangeFilter.between(startTime, endTime),
                        Collections.emptySet()
                );

                AggregationResult response = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, continuation) -> healthConnectClient.aggregate(request, continuation)
                );

                Long totalSteps = response.get(StepsRecord.COUNT_TOTAL);
                long steps = (totalSteps != null) ? totalSteps : 0;

                callback.onSuccess(new StepData(steps));

            } catch (Exception e) {
                callback.onError(e);
            } finally {
                executor.shutdown();
            }
        });
    }
}