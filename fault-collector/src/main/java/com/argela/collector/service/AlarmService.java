package com.argela.collector.service;

import com.argela.collector.client.ProcessorClient;
import com.argela.collector.model.AlarmRequest;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AlarmService {

    private static final String IP_PATTERN = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
    private static final long MAX_AGE_MINUTES = 60;

    private final ProcessorClient processorClient;
    private final Tracer tracer;
    private final LongCounter receivedCounter;

    public AlarmService(ProcessorClient processorClient) {
        this.processorClient = processorClient;
        this.tracer = GlobalOpenTelemetry.getTracer("fault-collector");
        Meter meter = GlobalOpenTelemetry.getMeter("fault-collector");
        this.receivedCounter = meter.counterBuilder("alarms.received.total")
                .setDescription("Total number of alarms received")
                .setUnit("{alarm}")
                .build();
    }

    public Mono<String> validateAndForward(AlarmRequest request) {
        Span span = tracer.spanBuilder("alarm.validate")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("alarm.id", request.getAlarmId());
            span.setAttribute("alarm.type", request.getAlarmType().name());
            span.setAttribute("alarm.source_ip", request.getSourceIp());

            validate(request, span);

            receivedCounter.add(1, Attributes.of(
                    AttributeKey.stringKey("alarm.type"), request.getAlarmType().name()
            ));

            return processorClient.forwardAlarm(request)
                    .doFinally(signal -> span.end());
        } catch (IllegalArgumentException e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            span.end();
            return Mono.error(e);
        }
    }

    private void validate(AlarmRequest request, Span span) {
        if (!request.getSourceIp().matches(IP_PATTERN)) {
            throw new IllegalArgumentException("Invalid source IP format: " + request.getSourceIp());
        }

        long ageMinutes = ChronoUnit.MINUTES.between(request.getTimestamp(), LocalDateTime.now());
        if (ageMinutes > MAX_AGE_MINUTES) {
            throw new IllegalArgumentException("Alarm timestamp is too old: " + ageMinutes + " minutes");
        }

        span.setAttribute("alarm.age_minutes", ageMinutes);
    }
}
