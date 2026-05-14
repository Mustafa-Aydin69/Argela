package com.argela.processor.model;

public class ProcessResponse {

    private String alarmId;
    private String status;

    public ProcessResponse() {}

    public ProcessResponse(String alarmId, String status) {
        this.alarmId = alarmId;
        this.status = status;
    }

    public String getAlarmId() { return alarmId; }
    public void setAlarmId(String alarmId) { this.alarmId = alarmId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
