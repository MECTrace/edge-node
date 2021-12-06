package com.penta.edge.controller;

import com.penta.edge.service.TcpMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import java.time.LocalDateTime;

@MessageEndpoint
@RequiredArgsConstructor
@Slf4j
public class TcpEndpoint {

    private final TcpMessageService tcpMessageService;

    @ServiceActivator(inputChannel = "inboundChannel", async = "true")
    public void getMessageFromKETI(byte[] message) {

        // 데이터 수신시간 Timestamp
        LocalDateTime receivingTime = LocalDateTime.now();
        String receivedMsg = new String(message);

        log.info("Received message(Original Bytes) :: {}", message);
        log.info("Received message(String) :: {}", receivedMsg);

        // edge supporter -> edge tcp socket 통신을 위한 분기
        /*
        * central web에서 datafile download클릭 시 supporter API가 호출되고
        * supporter API는 본 API를(socket) 호출. 이후 tracing history를 생성하여 central로 해당 데이터 전송
        * */

        if(receivedMsg.contains("sptoedge9812")) {
            tcpMessageService.getDataFromSupporter(message);
        } else {
            tcpMessageService.getMessageFromKETI(message, receivingTime);
        }

    }


}
