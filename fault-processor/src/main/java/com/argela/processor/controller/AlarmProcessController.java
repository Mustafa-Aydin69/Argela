package com.argela.processor.controller;

import com.argela.processor.model.AlarmRequest;
import com.argela.processor.model.ProcessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/process")
public class AlarmProcessController {

    @PostMapping
    public ResponseEntity<ProcessResponse> processAlarm(@RequestBody AlarmRequest request) {
        ProcessResponse response = new ProcessResponse(request.getAlarmId(), "RECEIVED");
        return ResponseEntity.ok(response);
    }
}
