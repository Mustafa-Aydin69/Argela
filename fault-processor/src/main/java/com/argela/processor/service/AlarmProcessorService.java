package com.argela.processor.service;

import com.argela.processor.entity.Alarm;
import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmStatus;
import com.argela.processor.model.ProcessResponse;
import com.argela.processor.model.SeverityLevel;
import com.argela.processor.repository.AlarmRepository;
import com.argela.processor.service.severity.SeverityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AlarmProcessorService {

    private final AlarmRepository repository;
    private final SeverityService severityService;

    public AlarmProcessorService(AlarmRepository repository, SeverityService severityService) {
        this.repository = repository;
        this.severityService = severityService;
    }

    @Transactional
    public ProcessResponse process(AlarmRequest request) {
        Alarm alarm = new Alarm();
        alarm.setAlarmId(request.getAlarmId());
        alarm.setSourceIp(request.getSourceIp());
        alarm.setAlarmType(request.getAlarmType());
        alarm.setStatus(AlarmStatus.RECEIVED);
        alarm.setCreatedAt(LocalDateTime.now());

        alarm.setStatus(AlarmStatus.PROCESSING);
        SeverityLevel severity = severityService.resolve(request);
        alarm.setSeverityLevel(severity);

        alarm.setStatus(AlarmStatus.PROCESSED);
        alarm.setProcessedAt(LocalDateTime.now());

        Alarm saved = repository.save(alarm);
        return new ProcessResponse(saved.getAlarmId(), saved.getStatus(), saved.getSeverityLevel());
    }
}
