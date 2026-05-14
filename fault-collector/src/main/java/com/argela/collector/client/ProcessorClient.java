package com.argela.collector.client;

import com.argela.collector.model.AlarmRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class ProcessorClient {

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
                .onErrorResume(ex -> Mono.just("{\"alarmId\":\"" + request.getAlarmId() + "\",\"status\":\"FALLBACK\"}"));
    }
}
