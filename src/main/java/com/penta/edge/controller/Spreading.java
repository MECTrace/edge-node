package com.penta.edge.controller;


import com.penta.edge.constant.EdgeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@RequiredArgsConstructor
@Slf4j
@Component
public class Spreading {

    private final EdgeInfo edgeInfo;

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    public void getDeviceInfo() {

        log.info("--------getDeviceInfo---------!!!!!");

        try {
            // 서버에 요청 보내기
            socket = new Socket(edgeInfo.getIP(), edgeInfo.getTCP_PORT());
            System.out.println(socket.getInetAddress().getHostAddress() + "에 연결됨!!");

            // 메시지 받기
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream());

            // 메세지 전달
            pw.println("{[{REQ::10.0.7.11::001:: EDGE_LIST}]}");
            pw.flush();

            // 응답 출력
            System.out.println("응답---------");
            System.out.println(br.readLine());
        } catch (IOException e) {
            System.out.println("연결실패");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            // 소켓 닫기 (연결 끊기)
            try {
                if(socket != null) { socket.close(); }
                if(br != null) { br.close(); }
                if(pw != null) { pw.close(); }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }


}
