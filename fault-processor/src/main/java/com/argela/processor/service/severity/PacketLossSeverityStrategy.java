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

    private static final int HIGH_THRESHOLD_PERCENT = 20;

    @Override
    public SeverityLevel calculate(AlarmRequest request) {
        if (request.getMetrics() != null && request.getMetrics().getPacketLossPercent() != null) {
            return request.getMetrics().getPacketLossPercent() > HIGH_THRESHOLD_PERCENT
                    ? SeverityLevel.HIGH : SeverityLevel.MEDIUM;
        }
        return SeverityLevel.MEDIUM;
    }
}
