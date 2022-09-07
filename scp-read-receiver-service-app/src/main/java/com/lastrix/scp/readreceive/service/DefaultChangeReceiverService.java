package com.lastrix.scp.readreceive.service;

import com.lastrix.scp.receiver.ChangeReceiver;
import com.lastrix.scp.receiver.ChangeReceiverService;
import com.lastrix.scp.receiver.ChangeSinkService;
import com.lastrix.scp.writesender.model.EnrolleeSelect;
import com.lastrix.scp.writesender.model.EnrolleeSelectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DefaultChangeReceiverService extends ChangeReceiverService<EnrolleeSelect> {
    public DefaultChangeReceiverService(
            ChangeSinkService<EnrolleeSelect> sink,
            ChangeReceiver<EnrolleeSelect> receiver,
            @Value("${scp.rrs.receiver.chunk-size}") int sinkChunkSize,
            @Value("${scp.rrs.receiver.max-chunk-size}") int maxSinkChunkSize,
            @Value("${scp.rrs.receiver.receive-buffer-size}") int receiveBufferSize) {
        super(sink, receiver, sinkChunkSize, maxSinkChunkSize, receiveBufferSize);
    }

    @Override
    protected Object idOf(EnrolleeSelect c) {
        return new EnrolleeSelectId(c.getUserId(), c.getSessionId(), c.getSpecId(), c.getOrdinal());
    }
}
