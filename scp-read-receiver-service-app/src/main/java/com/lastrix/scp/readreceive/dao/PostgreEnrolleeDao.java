package com.lastrix.scp.readreceive.dao;

import com.lastrix.scp.readreceive.model.EnrolleeSelectId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
public class PostgreEnrolleeDao implements EnrolleeDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgreEnrolleeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int commit(List<EnrolleeSelectId> changes) {
        int[] a = jdbcTemplate.batchUpdate(
                "UPDATE scp_read_service.enrollee_select SET state = true, modified_stamp = CURRENT_TIMESTAMP WHERE user_id = ? AND session_id = ? AND spec_id = ? AND ordinal = ?",
                changes.stream().map(x -> new Object[]{x.getUserId(), x.getSessionId(), x.getSpecId(), x.getOrdinal()}).toList()
        );
        int t = 0;
        for (int v : a) {
            t += v;
        }
        return t;
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
}
