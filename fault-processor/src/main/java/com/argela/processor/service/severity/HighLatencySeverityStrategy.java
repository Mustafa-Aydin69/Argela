package com.argela.processor.service.severity;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HighLatencySeverityStrategy implements SeverityStrategy {

    private static final Pattern MS_PATTERN = Pattern.compile("(\\d+)\\s*ms");
    private static final int HIGH_THRESHOLD_MS = 500;

    @Override
    public AlarmType supportedType() {
        return AlarmType.HIGH_LATENCY;
    }

    @Override
    public SeverityLevel calculate(AlarmRequest request) {
        String desc = request.getDescription() == null ? "" : request.getDescription();
        Matcher matcher = MS_PATTERN.matcher(desc);
        if (matcher.find()) {
            int latencyMs = Integer.parseInt(matcher.group(1));
            return latencyMs > HIGH_THRESHOLD_MS ? SeverityLevel.HIGH : SeverityLevel.MEDIUM;
        }
        return SeverityLevel.MEDIUM;
    }
}
