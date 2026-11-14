CREATE TABLE IF NOT EXISTS readings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_kind VARCHAR(16) NOT NULL,
    recorded_at DATETIME NOT NULL,
    power_kw DECIMAL(8, 3) NOT NULL,
    energy_kwh DECIMAL(8, 3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_reading_hour (source_kind, recorded_at)
);
