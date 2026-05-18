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

    @Override
    public SeverityLevel calculate(AlarmRequest request) {
        String desc = request.getDescription() == null ? "" : request.getDescription().toLowerCase();
        if (desc.contains("peak")) {
            return SeverityLevel.HIGH;
        }
        return SeverityLevel.MEDIUM;
    }
}
