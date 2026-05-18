package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeverityService {

    private final Map<AlarmType, SeverityStrategy> strategies;
    private final Tracer tracer;

    public SeverityService(List<SeverityStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(SeverityStrategy::supportedType, s -> s));
        this.tracer = GlobalOpenTelemetry.getTracer("fault-processor");
    }

    public SeverityLevel resolve(AlarmRequest request) {
        Span span = tracer.spanBuilder("severity.calculate")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("alarm.id", request.getAlarmId());
            span.setAttribute("alarm.type", request.getAlarmType().name());

            SeverityStrategy strategy = strategies.get(request.getAlarmType());
            if (strategy == null) {
                throw new IllegalStateException("No severity strategy registered for type: " + request.getAlarmType());
            }

            SeverityLevel severity = strategy.calculate(request);
            span.setAttribute("alarm.severity", severity.name());
            return severity;
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
