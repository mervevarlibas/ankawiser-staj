package com.mwitter.monitoring;

import java.io.IOException;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpRequestMetrics extends OncePerRequestFilter {

    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();
    private final LongAdder totalDurationNanos = new LongAdder();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.nanoTime();//her istekte ile başlangıç zamanı alınır.
        try {
            filterChain.doFilter(request, response);
        } finally {
            totalRequests.increment();//istek tamamnlanınca total istek sayısı artırılır
            totalDurationNanos.add(System.nanoTime() - startedAt);//istek süresi kaydedilir
            if (response.getStatus() >= 400) {
                failedRequests.increment();
            }
        }
    }

    public Snapshot snapshot() {//Bu değerler uygulama yeniden başlatılınca sıfırlanır. Çünkü şu anda MongoDB’ye kaydedilmiyor, RAM’de tutuluyor.
        long total = totalRequests.sum();
        long failed = failedRequests.sum();
        double averageMs = total == 0 ? 0 : totalDurationNanos.sum() / 1_000_000.0 / total;
        double errorRate = total == 0 ? 0 : failed * 100.0 / total;
        return new Snapshot(total, failed, round(averageMs), round(errorRate));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public record Snapshot(long totalRequests, long failedRequests, double averageResponseMs, double errorRatePercent) {
    }
}
