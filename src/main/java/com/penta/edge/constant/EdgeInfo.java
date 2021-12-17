package com.penta.edge.constant;

import com.penta.edge.configuration.EdgeInfoProperties;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class EdgeInfo {

    private final String uuid;
    private final String IP;

    @SneakyThrows
    public EdgeInfo(EdgeInfoProperties edgeInfoProperties) {
        this.uuid = edgeInfoProperties.getUuid();
        this.IP = edgeInfoProperties.getIp();
        log.info("------- Edge Information -------");
        log.info("UUID(name) :: {}", this.uuid);
        log.info("SERVER IP :: {}", this.IP);
        log.info("------- Edge Information -------");
    }

}
