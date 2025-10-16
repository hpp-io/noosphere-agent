package io.hpp.noosphere.agent.service.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BlockChainService {

    private static final Logger log = LoggerFactory.getLogger(BlockChainService.class);

    public BlockChainService() {
        log.info("Initialized ChainProcessorService");
    }
}
