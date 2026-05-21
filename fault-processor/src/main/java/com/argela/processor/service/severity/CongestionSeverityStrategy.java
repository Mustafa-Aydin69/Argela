package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import org.springframework.stereotype.Component;

@Component
public class CongestionSeverityStrategy implements SeverityStrategy {

    @Override
    public AlarmType supportedType() {
        return AlarmType.CONGESTION;
    }

    private static final int HIGH_THRESHOLD_PERCENT = 80;

    @Override
    public SeverityLevel calculate(AlarmRequest request) {
        if (request.getMetrics() != null && request.getMetrics().getBandwidthUtilizationPercent() != null) {
            return request.getMetrics().getBandwidthUtilizationPercent() > HIGH_THRESHOLD_PERCENT
                    ? SeverityLevel.HIGH : SeverityLevel.MEDIUM;
        }
        return SeverityLevel.MEDIUM;
    }
}
