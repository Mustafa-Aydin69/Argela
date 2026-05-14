package com.argela.collector.client;

import com.argela.collector.model.AlarmRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .bodyToMono(String.class);
    }
}
