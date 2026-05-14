package com.argela.collector.controller;

import com.argela.collector.model.AlarmRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmController {

    @PostMapping
    public ResponseEntity<String> receiveAlarm(@Valid @RequestBody AlarmRequest request) {
        return ResponseEntity.accepted().body("Alarm received: " + request.getAlarmId());
    }
}
