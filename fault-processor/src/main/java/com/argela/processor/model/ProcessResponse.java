package com.argela.processor.model;

public class ProcessResponse {

    private String alarmId;
    private AlarmStatus status;
    private SeverityLevel severityLevel;

    public ProcessResponse() {}

    public ProcessResponse(String alarmId, AlarmStatus status, SeverityLevel severityLevel) {
        this.alarmId = alarmId;
        this.status = status;
        this.severityLevel = severityLevel;
    }

    public String getAlarmId() { return alarmId; }
    public void setAlarmId(String alarmId) { this.alarmId = alarmId; }

    public AlarmStatus getStatus() { return status; }
    public void setStatus(AlarmStatus status) { this.status = status; }

    public SeverityLevel getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(SeverityLevel severityLevel) { this.severityLevel = severityLevel; }
}
