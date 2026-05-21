package com.argela.processor.model;

import java.time.LocalDateTime;

public class AlarmRequest {

    private String alarmId;
    private String sourceIp;
    private AlarmType alarmType;
    private String description;
    private LocalDateTime timestamp;
    private AlarmMetrics metrics;

    public AlarmRequest() {}

    public String getAlarmId() { return alarmId; }
    public void setAlarmId(String alarmId) { this.alarmId = alarmId; }

    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }

    public AlarmType getAlarmType() { return alarmType; }
    public void setAlarmType(AlarmType alarmType) { this.alarmType = alarmType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public AlarmMetrics getMetrics() { return metrics; }
    public void setMetrics(AlarmMetrics metrics) { this.metrics = metrics; }
}
