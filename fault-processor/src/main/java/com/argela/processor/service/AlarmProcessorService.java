package com.argela.processor.service;

import com.argela.processor.entity.Alarm;
import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmStatus;
import com.argela.processor.model.ProcessResponse;
import com.argela.processor.model.SeverityLevel;
import com.argela.processor.repository.AlarmRepository;
import com.argela.processor.service.severity.SeverityService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AlarmProcessorService {

    private final AlarmRepository repository;
    private final SeverityService severityService;
    private final Tracer tracer;

    public AlarmProcessorService(AlarmRepository repository, SeverityService severityService) {
        this.repository = repository;
        this.severityService = severityService;
        this.tracer = GlobalOpenTelemetry.getTracer("fault-processor");
    }

    @Transactional
    public ProcessResponse process(AlarmRequest request) {
        Span span = tracer.spanBuilder("alarm.process")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("alarm.id", request.getAlarmId());
            span.setAttribute("alarm.type", request.getAlarmType().name());
            span.setAttribute("alarm.source_ip", request.getSourceIp());

            Alarm alarm = new Alarm();
            alarm.setAlarmId(request.getAlarmId());
            alarm.setSourceIp(request.getSourceIp());
            alarm.setAlarmType(request.getAlarmType());
            alarm.setStatus(AlarmStatus.RECEIVED);
            alarm.setCreatedAt(LocalDateTime.now());

            alarm.setStatus(AlarmStatus.PROCESSING);
            SeverityLevel severity = severityService.resolve(request); // severity.calculate buradan child span açar
            alarm.setSeverityLevel(severity);
            span.setAttribute("alarm.severity", severity.name());

            alarm.setStatus(AlarmStatus.PROCESSED);
            alarm.setProcessedAt(LocalDateTime.now());

            Alarm saved = repository.save(alarm); // DB span'ları da alarm.process'in child'ı olur
            return new ProcessResponse(saved.getAlarmId(), saved.getStatus(), saved.getSeverityLevel());
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
