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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AlarmService {

    private static final Logger log = LoggerFactory.getLogger(AlarmService.class);

    private static final String IP_PATTERN = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
    private static final long WARN_AGE_MINUTES = 30;
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

            span.addEvent("alarm.validated", Attributes.of(
                    AttributeKey.stringKey("alarm.id"), request.getAlarmId(),
                    AttributeKey.stringKey("alarm.type"), request.getAlarmType().name(),
                    AttributeKey.stringKey("alarm.source_ip"), request.getSourceIp()
            ));
            // INFO: alarm doğrulama başarılı, processor'a iletiliyor
            log.atInfo()
                    .addKeyValue("alarm.id", request.getAlarmId())
                    .addKeyValue("alarm.type", request.getAlarmType().name())
                    .addKeyValue("alarm.source_ip", request.getSourceIp())
                    .log("Alarm validated and forwarded to processor");

            return processorClient.forwardAlarm(request)
                    .doFinally(signal -> span.end());
        } catch (IllegalArgumentException e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            span.end();
            // ERROR: doğrulama başarısız
            log.atError()
                    .addKeyValue("alarm.id", request.getAlarmId())
                    .addKeyValue("alarm.source_ip", request.getSourceIp())
                    .setCause(e)
                    .log("Alarm validation failed");
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

        // WARN: alarm yaşı eşiği aştı ama henüz geçersiz değil
        if (ageMinutes > WARN_AGE_MINUTES) {
            span.addEvent("alarm.aging", Attributes.of(
                    AttributeKey.longKey("alarm.age_minutes"), ageMinutes,
                    AttributeKey.longKey("warn_threshold_minutes"), WARN_AGE_MINUTES
            ));
            log.atWarn()
                    .addKeyValue("alarm.id", request.getAlarmId())
                    .addKeyValue("alarm.age_minutes", ageMinutes)
                    .addKeyValue("warn_threshold_minutes", WARN_AGE_MINUTES)
                    .log("Alarm is aging — approaching expiry threshold");
        }

        span.setAttribute("alarm.age_minutes", ageMinutes);
    }
}
