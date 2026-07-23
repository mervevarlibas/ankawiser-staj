package com.mwitter.service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mwitter.dto.AdminMetricsResponse;
import com.mwitter.monitoring.HttpRequestMetrics;

@Service
public class AdminMetricsService {

    private final MongoTemplate mongoTemplate;
    private final HttpRequestMetrics requestMetrics;
    private final com.sun.management.OperatingSystemMXBean operatingSystem;

    public AdminMetricsService(MongoTemplate mongoTemplate, HttpRequestMetrics requestMetrics) {
        this.mongoTemplate = mongoTemplate;
        this.requestMetrics = requestMetrics;
        this.operatingSystem = ManagementFactory.getPlatformMXBean(
                com.sun.management.OperatingSystemMXBean.class);
    }

    public AdminMetricsResponse currentMetrics() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        var threads = ManagementFactory.getThreadMXBean();
        var runtime = ManagementFactory.getRuntimeMXBean();
        HttpRequestMetrics.Snapshot http = requestMetrics.snapshot();

        long totalMemory = operatingSystem == null ? -1 : operatingSystem.getTotalMemorySize();
        long freeMemory = operatingSystem == null ? -1 : operatingSystem.getFreeMemorySize();
        long usedMemory = totalMemory < 0 || freeMemory < 0 ? -1 : totalMemory - freeMemory;

        long diskTotal = -1;
        long diskUsed = -1;
        try {
            FileStore store = Files.getFileStore(Path.of(".").toAbsolutePath());
            diskTotal = store.getTotalSpace();
            diskUsed = diskTotal - store.getUsableSpace();
        } catch (Exception ignored) {
            // Bazı container sağlayıcıları disk bilgisini sınırlar; panel "kullanılamıyor" gösterir.
        }

        String databaseStatus = databaseStatus();
        String overallStatus = "UP".equals(databaseStatus) ? "UP" : "DEGRADED";

        return new AdminMetricsResponse(
                Instant.now(),
                overallStatus,
                databaseStatus,
                serverName(),
                ProcessHandle.current().pid(),
                Runtime.getRuntime().availableProcessors(),
                percent(operatingSystem == null ? -1 : operatingSystem.getCpuLoad()),
                percent(operatingSystem == null ? -1 : operatingSystem.getProcessCpuLoad()),
                usedMemory,
                totalMemory,
                heap.getUsed(),
                heap.getMax(),
                diskUsed,
                diskTotal,
                runtime.getUptime() / 1000,
                threads.getThreadCount(),
                threads.getPeakThreadCount(),
                http.totalRequests(),
                http.failedRequests(),
                http.errorRatePercent(),
                http.averageResponseMs());
    }

    private String databaseStatus() {
        try {
            Document result = mongoTemplate.executeCommand("{ ping: 1 }");
            return result.getDouble("ok") == 1.0 ? "UP" : "DOWN";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private static String serverName() {
        for (String variable : new String[] { "RENDER_INSTANCE_ID", "HOSTNAME", "COMPUTERNAME" }) {
            String value = System.getenv(variable);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "unknown";
        }
    }

    private static double percent(double ratio) {
        return ratio < 0 ? -1 : Math.round(ratio * 1000.0) / 10.0;
    }
}
