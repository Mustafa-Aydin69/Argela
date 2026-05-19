package com.argela.processor.service;

import com.argela.processor.entity.Alarm;
import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmStatus;
import com.argela.processor.model.ProcessResponse;
import com.argela.processor.model.SeverityLevel;
import com.argela.processor.repository.AlarmRepository;
import com.argela.processor.service.severity.SeverityService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AlarmProcessorService {

    private static final Logger log = LoggerFactory.getLogger(AlarmProcessorService.class);

    private final AlarmRepository repository;
    private final SeverityService severityService;
    private final Tracer tracer;
    private final LongCounter processedCounter;
    private final DoubleHistogram processingDurationHistogram;

    public AlarmProcessorService(AlarmRepository repository, SeverityService severityService) {
        this.repository = repository;
        this.severityService = severityService;
        this.tracer = GlobalOpenTelemetry.getTracer("fault-processor");
        Meter meter = GlobalOpenTelemetry.getMeter("fault-processor");
        this.processedCounter = meter.counterBuilder("alarms.processed.total")
                .setDescription("Total number of alarms successfully processed")
                .setUnit("{alarm}")
                .build();
        this.processingDurationHistogram = meter.histogramBuilder("alarm.processing.duration")
                .setDescription("Time taken to process an alarm end-to-end")
                .setUnit("ms")
                .build();
        meter.gaugeBuilder("alarms.pending.count")
                .ofLongs()
                .setDescription("Number of alarms currently in PROCESSING state")
                .setUnit("{alarm}")
                .buildWithCallback(measurement ->
                        measurement.record(repository.countByStatus(AlarmStatus.PROCESSING))
                );
    }

    @Transactional
    public ProcessResponse process(AlarmRequest request) {
        Span span = tracer.spanBuilder("alarm.process")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        long startNano = System.nanoTime();
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

            // WARN: kritik alarm, operasyon ekibi bilgilendirilmeli
            if (severity == SeverityLevel.CRITICAL) {
                log.atWarn()
                        .addKeyValue("alarm.id", request.getAlarmId())
                        .addKeyValue("alarm.type", request.getAlarmType().name())
                        .addKeyValue("alarm.source_ip", request.getSourceIp())
                        .addKeyValue("severity", severity.name())
                        .log("CRITICAL severity alarm detected — immediate attention required");
            }

            alarm.setStatus(AlarmStatus.PROCESSED);
            alarm.setProcessedAt(LocalDateTime.now());

            Alarm saved = repository.save(alarm); // DB span'ları da alarm.process'in child'ı olur
            double elapsedMs = (System.nanoTime() - startNano) / 1_000_000.0;
            processedCounter.add(1, Attributes.of(
                    AttributeKey.stringKey("severity.level"), severity.name()
            ));
            processingDurationHistogram.record(elapsedMs, Attributes.of(
                    AttributeKey.stringKey("alarm.type"), request.getAlarmType().name()
            ));

            logAlarmRecord(saved, elapsedMs);

            return new ProcessResponse(saved.getAlarmId(), saved.getStatus(), saved.getSeverityLevel());
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            // ERROR: işlem sırasında beklenmedik hata
            log.atError()
                    .addKeyValue("alarm.id", request.getAlarmId())
                    .addKeyValue("alarm.type", request.getAlarmType().name())
                    .setCause(e)
                    .log("Alarm processing failed unexpectedly");
            throw e;
        } finally {
            span.end();
        }
    }

    private void logAlarmRecord(Alarm saved, double elapsedMs) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        log.atInfo()
                .addKeyValue("alarm.db_id",        saved.getId())
                .addKeyValue("alarm.id",            saved.getAlarmId())
                .addKeyValue("alarm.type",          saved.getAlarmType().name())
                .addKeyValue("alarm.source_ip",     saved.getSourceIp())
                .addKeyValue("alarm.severity",      saved.getSeverityLevel().name())
                .addKeyValue("alarm.status",        saved.getStatus().name())
                .addKeyValue("alarm.created_at",    saved.getCreatedAt().format(fmt))
                .addKeyValue("alarm.processed_at",  saved.getProcessedAt().format(fmt))
                .addKeyValue("alarm.duration_ms",   Math.round(elapsedMs))
                .log("Alarm record persisted");
    }
}
