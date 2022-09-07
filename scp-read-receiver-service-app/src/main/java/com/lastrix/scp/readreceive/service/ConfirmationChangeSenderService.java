package com.lastrix.scp.readreceive.service;

import com.lastrix.scp.readreceive.model.EnrolleeSelectId;
import com.lastrix.scp.sender.ChangeSender;
import com.lastrix.scp.sender.ChangeSenderService;
import com.lastrix.scp.sender.ChangeSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationChangeSenderService extends ChangeSenderService<EnrolleeSelectId> {
    private static final Logger log = LoggerFactory.getLogger(ConfirmationChangeSenderService.class);
    private final int channelStart;
    private final int channelEnd;
    private final int channelMask;

    @Autowired
    public ConfirmationChangeSenderService(
            ChangeSourceService<EnrolleeSelectId> source,
            ChangeSender<EnrolleeSelectId> sender,
            @Value("${scp.rrs.worker.parallelism}") int parallelism,
            @Value("${scp.rrs.worker.channels.count}") int channels,
            @Value("${scp.rrs.worker.max-processing-chunk}") int maxProcessingChunk,
            @Value("${scp.rrs.worker.channels.start}") int channelStart,
            @Value("${scp.rrs.worker.channels.end}") int channelEnd,
            @Value("${scp.rrs.worker.channels.mask}") int channelMask) {
        super(source, sender, parallelism, channels, maxProcessingChunk);
        this.channelStart = channelStart;
        this.channelEnd = channelEnd;
        this.channelMask = channelMask;
    }

    @Override
    protected Object idOf(EnrolleeSelectId o) {
        // we're already working with ids
        return o;
    }

    @Override
    protected int channelOf(EnrolleeSelectId o) {
        var channel = (int) o.getUserId().getLeastSignificantBits() & channelMask;
        if (channel < channelStart || channel >= channelEnd) {
            // we should not throw errors here, just warn log that something bad happens with database
            // or our configuration
            log.warn("Wrong channel usage detected in database for {}:{}:{}", o.getUserId(), o.getSessionId(), o.getSpecId());
        }
        return channel;
    }

}
