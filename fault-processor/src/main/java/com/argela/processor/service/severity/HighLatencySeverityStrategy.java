package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import org.springframework.stereotype.Component;

@Component
public class HighLatencySeverityStrategy implements SeverityStrategy {

    private static final int HIGH_THRESHOLD_MS = 500;

    @Override
    public AlarmType supportedType() {
        return AlarmType.HIGH_LATENCY;
    }

    @Override
    public SeverityLevel calculate(AlarmRequest request) {
        if (request.getMetrics() != null && request.getMetrics().getLatencyMs() != null) {
            return request.getMetrics().getLatencyMs() > HIGH_THRESHOLD_MS
                    ? SeverityLevel.HIGH : SeverityLevel.MEDIUM;
        }
        return SeverityLevel.MEDIUM;
    }
}
