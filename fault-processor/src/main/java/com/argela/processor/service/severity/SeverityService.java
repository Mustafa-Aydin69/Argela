package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeverityService {

    private final Map<AlarmType, SeverityStrategy> strategies;

    public SeverityService(List<SeverityStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(SeverityStrategy::supportedType, s -> s));
    }

    public SeverityLevel resolve(AlarmRequest request) {
        SeverityStrategy strategy = strategies.get(request.getAlarmType());
        if (strategy == null) {
            throw new IllegalStateException("No severity strategy registered for type: " + request.getAlarmType());
        }
        return strategy.calculate(request);
    }
}
