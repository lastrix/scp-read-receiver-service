package com.lastrix.scp.readreceive.service;

import com.lastrix.scp.readreceive.dao.EnrolleeDao;
import com.lastrix.scp.receiver.ChangeSinkService;
import com.lastrix.scp.writesender.model.EnrolleeSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultChangeSinkService implements ChangeSinkService<EnrolleeSelect> {
    private static final Logger log = LoggerFactory.getLogger(DefaultChangeSinkService.class);
    private final EnrolleeDao dao;

    public DefaultChangeSinkService(EnrolleeDao dao) {
        this.dao = dao;
    }

    @Override
    public void commit(List<EnrolleeSelect> changes) {
        var c = dao.update(changes);
        if (c != changes.size()) {
            log.warn("Fewer records updated than expected {} of {}", c, changes.size());
        }
    }
}
