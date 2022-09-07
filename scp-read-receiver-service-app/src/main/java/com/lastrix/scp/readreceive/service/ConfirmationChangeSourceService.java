package com.lastrix.scp.readreceive.service;

import com.lastrix.scp.readreceive.dao.EnrolleeDao;
import com.lastrix.scp.sender.ChangeSourceService;
import com.lastrix.scp.writesender.model.EnrolleeSelectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfirmationChangeSourceService implements ChangeSourceService<EnrolleeSelectId> {
    private static final Logger log = LoggerFactory.getLogger(ConfirmationChangeSourceService.class);

    private final EnrolleeDao dao;

    public ConfirmationChangeSourceService(EnrolleeDao dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EnrolleeSelectId> fetch(int page) {
        return dao.fetch(page);
    }

    @Transactional
    @Override
    public void commit(List<EnrolleeSelectId> changes) {
        // we may change fewer entries than expected
        // because of modification
        var c = dao.commit(changes);
        if (c != changes.size()) {
            log.trace("Fewer changes than expected: {}, should be {}", c, changes.size());
        }
    }
}
