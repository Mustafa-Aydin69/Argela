package com.argela.collector.controller;

import com.argela.collector.client.ProcessorClient;
import com.argela.collector.model.AlarmRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmController {

    private final ProcessorClient processorClient;

    public AlarmController(ProcessorClient processorClient) {
        this.processorClient = processorClient;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> receiveAlarm(@Valid @RequestBody AlarmRequest request) {
        return processorClient.forwardAlarm(request)
                .map(response -> ResponseEntity.accepted().body(response));
    }
}
