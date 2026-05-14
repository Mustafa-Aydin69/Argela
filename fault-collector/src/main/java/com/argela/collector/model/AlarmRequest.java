package com.argela.collector.model;

import java.time.LocalDateTime;

public class AlarmRequest {

    private String alarmId;
    private String sourceIp;
    private AlarmType alarmType;
    private String description;
    private LocalDateTime timestamp;

    public AlarmRequest() {}

    public AlarmRequest(String alarmId, String sourceIp, AlarmType alarmType,
                        String description, LocalDateTime timestamp) {
        this.alarmId = alarmId;
        this.sourceIp = sourceIp;
        this.alarmType = alarmType;
        this.description = description;
        this.timestamp = timestamp;
    }

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
}
