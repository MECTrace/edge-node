package com.penta.edge.service;

import com.penta.edge.constant.EdgeInfo;
import com.penta.edge.constant.EdgeNode;
import com.penta.edge.domain.Hash;
import com.penta.edge.process.EdgeProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcpMessageService {

    private final EdgeInfo edgeInfo;
    private final HashService hashService;
    private final EdgeProcess edgeProcess;

    public void getMessageFromKETI(byte[] message, LocalDateTime receivingTime) {

        // {[{ANS::ID::ANS_CODE::ANS_DATA::REC_DATA}]}
        // {[{ANS::송신EDGE IP::007::(Successes/Fail/Receive)::dataid}]}

        String receivedMsg = new String(message);
        String[] msgArr = receivedMsg.split("::");

        String sourceId = Arrays.stream(EdgeNode.values())
                .filter(edgeNode -> edgeNode.getIP().equals(msgArr[1].trim()))
                .findFirst()
                .orElseThrow().getUUID();

        hashService.save(Hash.builder()
                .dataId(msgArr[4])
                .sourceId(sourceId)
                .destinationId(edgeInfo.getName())
                .timestamp(receivingTime)
                .build());

    }

    /* supporter to edge */
    public void getDataFromSupporter(byte[] message) {
        /*
        * message format
        * {[{sptoedge9812::DATA_ID::RECEIVED_TIME::CLIENT_IP::}]}
        * */

        String receivedMsg = new String(message);
        String[] data = receivedMsg.split("::");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime datetime = LocalDateTime.parse(data[2],formatter);

        edgeProcess.saveAndsendDownloadHashToCentral(
                Hash.builder()
                        .dataId(data[1])
                        .timestamp(datetime)
                        .sourceId(data[3])
                        .destinationId(edgeInfo.getName())
                        .build()
        );




    }
}
