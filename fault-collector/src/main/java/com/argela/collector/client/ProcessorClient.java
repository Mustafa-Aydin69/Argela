package com.argela.collector.client;

import com.argela.collector.model.AlarmRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class ProcessorClient {

    private static final Logger log = LoggerFactory.getLogger(ProcessorClient.class);

    private final WebClient webClient;

    public ProcessorClient(WebClient processorWebClient) {
        this.webClient = processorWebClient;
    }

    public Mono<String> forwardAlarm(AlarmRequest request) {
        return webClient.post()
                .uri("/api/v1/process")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
                .onErrorResume(ex -> {
                    // WARN: tüm retry'lar tükendi, fallback devreye girdi
                    log.atWarn()
                            .addKeyValue("alarm.id", request.getAlarmId())
                            .addKeyValue("error", ex.getMessage())
                            .log("Processor unreachable after retries — returning fallback response");
                    return Mono.just("{\"alarmId\":\"" + request.getAlarmId() + "\",\"status\":\"FALLBACK\"}");
                });
    }
}
