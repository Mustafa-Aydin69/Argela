package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import org.springframework.stereotype.Component;

@Component
public class PacketLossSeverityStrategy implements SeverityStrategy {

    @Override
    public AlarmType supportedType() {
        return AlarmType.PACKET_LOSS;
    }

    @Override
    public SeverityLevel calculate(AlarmRequest request) {
        String desc = description(request);
        if (desc.contains("high") || desc.contains("severe")) {
            return SeverityLevel.HIGH;
        }
        return SeverityLevel.MEDIUM;
    }

    private String description(AlarmRequest request) {
        return request.getDescription() == null ? "" : request.getDescription().toLowerCase();
    }
}
