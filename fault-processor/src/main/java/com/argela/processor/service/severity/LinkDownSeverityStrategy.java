package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import org.springframework.stereotype.Component;

@Component
public class LinkDownSeverityStrategy implements SeverityStrategy {

    @Override
    public AlarmType supportedType() {
        return AlarmType.LINK_DOWN;
    }

    @Override
    public SeverityLevel calculate(AlarmRequest request) {
        return SeverityLevel.CRITICAL;
    }
}
