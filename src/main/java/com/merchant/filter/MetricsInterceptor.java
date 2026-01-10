package com.merchant.interceptor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Metrics Interceptor
 * Captures API request metrics for Prometheus
 */
@Component
@RequiredArgsConstructor
public class MetricsInterceptor implements HandlerInterceptor {
    
    private final MeterRegistry meterRegistry;
    private static final String START_TIME_ATTR = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Record request duration
            Timer.builder("http.server.requests")
                    .tag("method", request.getMethod())
                    .tag("uri", request.getRequestURI())
                    .tag("status", String.valueOf(response.getStatus()))
                    .register(meterRegistry)
                    .record(java.time.Duration.ofMillis(duration));
            
            // Count by status code
            Counter.builder("http.requests.total")
                    .tag("method", request.getMethod())
                    .tag("status", String.valueOf(response.getStatus()))
                    .register(meterRegistry)
                    .increment();
        }
    }
}
