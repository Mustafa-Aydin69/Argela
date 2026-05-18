package com.argela.processor.controller;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.ProcessResponse;
import com.argela.processor.service.AlarmProcessorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/process")
public class AlarmProcessController {

    private final AlarmProcessorService processorService;

    public AlarmProcessController(AlarmProcessorService processorService) {
        this.processorService = processorService;
    }

    @PostMapping
    public ResponseEntity<ProcessResponse> processAlarm(@Valid @RequestBody AlarmRequest request) {
        return ResponseEntity.ok(processorService.process(request));
    }
}
