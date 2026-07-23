package com.mwitter.dto;

import java.time.Instant;

public record AdminMetricsResponse(// değerlerin json olarak hazırlanması
                Instant measuredAt,
                String status,
                String databaseStatus,
                String serverName,
                long processId,
                int availableProcessors,
                double systemCpuPercent,
                double processCpuPercent,
                long serverMemoryUsedBytes,
                long serverMemoryTotalBytes,
                long jvmHeapUsedBytes,
                long jvmHeapMaxBytes,
                long diskUsedBytes,
                long diskTotalBytes,
                long uptimeSeconds,
                int liveThreads,
                int peakThreads,
                long totalRequests,
                long failedRequests,
                double errorRatePercent,
                double averageResponseMs) {
}
