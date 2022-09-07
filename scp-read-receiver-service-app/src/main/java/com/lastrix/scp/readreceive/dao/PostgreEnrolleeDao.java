package com.lastrix.scp.readreceive.dao;

import com.lastrix.scp.writesender.model.EnrolleeSelect;
import com.lastrix.scp.writesender.model.EnrolleeSelectId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
public class PostgreEnrolleeDao implements EnrolleeDao {
    public static final int[] EMPTY = new int[0];
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgreEnrolleeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int update(List<EnrolleeSelect> list) {
        List<EnrolleeSelect> insert = new ArrayList<>();
        List<EnrolleeSelect> update = new ArrayList<>();
        for (EnrolleeSelect es : list) {
            if (jdbcTemplate.queryForObject(
                    "SELECT EXISTS(SELECT true FROM scp_read_service.enrollee_select es WHERE user_id = ? AND session_id = ? AND spec_id = ?)",
                    new Object[]{es.getUserId(), es.getSessionId(), es.getSpecId()},
                    (rs, rn) -> rs.getBoolean(1))) {
                update.add(es);
            } else {
                insert.add(es);
            }
        }
        int[] i = insert.isEmpty() ? EMPTY : jdbcTemplate.batchUpdate("INSERT INTO scp_read_service.enrollee_select(status, score, created_stamp, confirmed_stamp, canceled_stamp, user_id, session_id, spec_id, ordinal)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                insert.stream().map(x -> new Object[]{
                        x.getStatus(),
                        x.getScore(),
                        fromInstantOrNull(x.getCreatedStamp()),
                        fromInstantOrNull(x.getConfirmedStamp()),
                        fromInstantOrNull(x.getCancelledStamp()),
                        x.getUserId(),
                        x.getSessionId(),
                        x.getSpecId(),
                        x.getOrdinal()
                }).toList());
        int[] a = update.isEmpty() ? EMPTY : jdbcTemplate.batchUpdate(
                "UPDATE scp_read_service.enrollee_select SET " +
                        "state = false, " +
                        "modified_stamp = CURRENT_TIMESTAMP, " +
                        "status = ?, " +
                        "score = ?, " +
                        "created_stamp = ?, " +
                        "confirmed_stamp = ?, " +
                        "canceled_stamp = ?, " +
                        "ordinal = ? " +
                        "WHERE user_id = ? AND session_id = ? AND spec_id = ? AND ordinal < ?",
                update.stream().map(x -> new Object[]{
                        x.getStatus(),
                        x.getScore(),
                        fromInstantOrNull(x.getCreatedStamp()),
                        fromInstantOrNull(x.getConfirmedStamp()),
                        fromInstantOrNull(x.getCancelledStamp()),
                        x.getOrdinal(),
                        x.getUserId(),
                        x.getSessionId(),
                        x.getSpecId(),
                        x.getOrdinal()
                }).toList()
        );
        return sumArray(i) + sumArray(a);
    }


    @Override
    public int commit(List<EnrolleeSelectId> changes) {
        int[] a = jdbcTemplate.batchUpdate(
                "UPDATE scp_read_service.enrollee_select SET state = true, modified_stamp = CURRENT_TIMESTAMP WHERE user_id = ? AND session_id = ? AND spec_id = ? AND ordinal = ?",
                changes.stream().map(x -> new Object[]{x.getUserId(), x.getSessionId(), x.getSpecId(), x.getOrdinal()}).toList()
        );
        return sumArray(a);
    }

    @Override
    public List<EnrolleeSelectId> fetch(int page) {
        return jdbcTemplate.query(
                "SELECT spec_id, session_id, user_id, ordinal " +
                        "FROM scp_read_service.enrollee_select es " +
                        "WHERE es.state = false " +
                        "ORDER BY es.modified_stamp " +
                        "LIMIT 128 OFFSET ?",
                (rs, rowNum) -> {
                    var e = new EnrolleeSelectId();
                    e.setSpecId(UUID.fromString(rs.getString(1)));
                    e.setSessionId(rs.getInt(2));
                    e.setUserId(UUID.fromString(rs.getString(3)));
                    e.setOrdinal(rs.getInt(4));
                    return e;
                },
                page * 128
        );
    }

    private int sumArray(int[] a) {
        int t = 0;
        for (int v : a) {
            t += v;
        }
        return t;
    }

    private Timestamp fromInstantOrNull(Instant i) {
        return i == null ? null : Timestamp.from(i);
    }
}
