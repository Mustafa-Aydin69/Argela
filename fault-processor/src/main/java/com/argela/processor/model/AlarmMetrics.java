package com.argela.processor.model;

public class AlarmMetrics {

    private Integer latencyMs;
    private Integer packetLossPercent;
    private Integer bandwidthUtilizationPercent;

    public AlarmMetrics() {}

    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }

    public Integer getPacketLossPercent() { return packetLossPercent; }
    public void setPacketLossPercent(Integer packetLossPercent) { this.packetLossPercent = packetLossPercent; }

    public Integer getBandwidthUtilizationPercent() { return bandwidthUtilizationPercent; }
    public void setBandwidthUtilizationPercent(Integer bandwidthUtilizationPercent) {
        this.bandwidthUtilizationPercent = bandwidthUtilizationPercent;
    }
}
