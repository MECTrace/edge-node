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

    private final String name;
    private final String IP;
    private final int TCP_PORT = 16300;

    @SneakyThrows
    public EdgeInfo(EdgeInfoProperties edgeInfoProperties) {
        this.name = edgeInfoProperties.getName();
        this.IP = edgeInfoProperties.getIp();
        log.info("------- Edge Information -------");
        log.info("UUID(name) :: {}", this.name);
        log.info("SERVER IP :: {}", this.IP);
        log.info("TCP_PORT :: {}", this.TCP_PORT);
        log.info("------- Edge Information -------");
    }

}
