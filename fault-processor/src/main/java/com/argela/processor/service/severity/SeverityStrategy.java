package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;

public interface SeverityStrategy {
    AlarmType supportedType();
    SeverityLevel calculate(AlarmRequest request);
}
