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


        // 해당 api로 들어오는 source는 모두 edge이기 때문에 "02"붙임
        String sourceId = "02"+ Arrays.stream(EdgeNode.values())
                .filter(edgeNode -> edgeNode.getIP().equals(msgArr[1].trim()))
                .findFirst()
                .orElseThrow().getUUID();

        Hash hash = Hash.builder()
                .dataId(msgArr[4])
                .sourceId(sourceId)
                .destinationId(edgeInfo.getName())
                .timestamp(receivingTime)
                .build();

        hashService.save(hash);
        edgeProcess.sendHashToCentral(hash);

    }

    /* supporter to edge */
    // central 웹에서 [다운로드]클릭시 호출됨
    public void getDataFromSupporter(byte[] message) {
        /*
        * message format
        * {[{sptoedge9812::DATA_ID::RECEIVED_TIME::CLIENT_IP::}]}
        * */

        String receivedMsg = new String(message);
        String[] data = receivedMsg.split("::");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime datetime = LocalDateTime.parse(data[2],formatter);

        /*
        * sourceID는 무조건 user >> IP 앞에 04 붙임
        * */
        edgeProcess.saveAndsendDownloadHashToCentral(
                Hash.builder()
                        .dataId(data[1])
                        .timestamp(datetime)
                        .sourceId("02"+edgeInfo.getName())
                        .destinationId("04"+data[3])
                        .build()
        );
    }

    /*
    * supporter로 부터 들어오는 데이터 삭제요청 처리
    * */
    public void deleteMetaAndHash(byte[] message) {

        /*
         * message format
         * {[{sptoedge1108::DATA_ID::}]}
         * */

        String receivedMsg = new String(message);
        String[] data = receivedMsg.split("::");

        edgeProcess.deleteMetaAndHash(data[1]);

    }



}
