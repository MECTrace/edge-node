package com.penta.edge.controller;

import com.penta.edge.service.TcpMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

@MessageEndpoint
@RequiredArgsConstructor
@Slf4j
public class TcpEndpoint {

    private final TcpMessageService tcpMessageService;

    @ServiceActivator(inputChannel = "inboundChannel", async = "true")
    public void getMessageFromKETI(byte[] message) {
        log.info("Received message(Original Bytes) :: {}", message);
        log.info("Received message(String) :: {}", new String(message));
        tcpMessageService.getMessageFromKETI(message);
    }


}
