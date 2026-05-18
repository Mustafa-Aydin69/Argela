CREATE TABLE alarms (
    id            BIGSERIAL    PRIMARY KEY,
    alarm_id      VARCHAR(255) NOT NULL UNIQUE,
    source_ip     VARCHAR(255) NOT NULL,
    alarm_type    VARCHAR(50)  NOT NULL,
    severity_level VARCHAR(50),
    status        VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    processed_at  TIMESTAMP
);
