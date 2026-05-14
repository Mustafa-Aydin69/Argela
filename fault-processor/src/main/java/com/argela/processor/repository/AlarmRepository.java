package com.argela.processor.repository;

import com.argela.processor.entity.Alarm;
import com.argela.processor.model.AlarmType;
import com.argela.processor.model.SeverityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Optional<Alarm> findByAlarmId(String alarmId);

    List<Alarm> findByAlarmType(AlarmType alarmType);

    List<Alarm> findBySeverityLevel(SeverityLevel severityLevel);

    List<Alarm> findByStatus(String status);
}
