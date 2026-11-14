package com.lumenfield.station.repository;

import com.lumenfield.station.model.Reading;
import com.lumenfield.station.model.SourceKind;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReadingRepository {

    private static final RowMapper<Reading> ROW = new RowMapper<>() {
        @Override
        public Reading mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Reading(
                    rs.getLong("id"),
                    SourceKind.valueOf(rs.getString("source_kind")),
                    rs.getTimestamp("recorded_at").toLocalDateTime(),
                    rs.getBigDecimal("power_kw"),
                    rs.getBigDecimal("energy_kwh"));
        }
    };

    private final JdbcTemplate jdbc;

    public ReadingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int count() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM readings", Integer.class);
        return count == null ? 0 : count;
    }

    public void upsert(SourceKind source, LocalDateTime hour, BigDecimal powerKw, BigDecimal energyKwh) {
        jdbc.update("""
                INSERT INTO readings (source_kind, recorded_at, power_kw, energy_kwh)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE power_kw = VALUES(power_kw), energy_kwh = VALUES(energy_kwh)
                """, source.name(), Timestamp.valueOf(hour), powerKw, energyKwh);
    }

    public void upsertAll(List<Object[]> batch) {
        jdbc.batchUpdate("""
                INSERT INTO readings (source_kind, recorded_at, power_kw, energy_kwh)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE power_kw = VALUES(power_kw), energy_kwh = VALUES(energy_kwh)
                """, batch);
    }

    public List<Reading> between(LocalDateTime from, LocalDateTime to) {
        return jdbc.query("""
                SELECT id, source_kind, recorded_at, power_kw, energy_kwh
                FROM readings
                WHERE recorded_at >= ? AND recorded_at < ?
                ORDER BY recorded_at, source_kind
                """, ROW, Timestamp.valueOf(from), Timestamp.valueOf(to));
    }

    public List<Reading> recent(int limit) {
        return jdbc.query("""
                SELECT id, source_kind, recorded_at, power_kw, energy_kwh
                FROM readings
                ORDER BY recorded_at DESC, source_kind
                LIMIT ?
                """, ROW, limit);
    }
}
