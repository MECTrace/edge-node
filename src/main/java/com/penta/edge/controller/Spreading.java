package com.penta.edge.controller;



// @RequiredArgsConstructor
// @Slf4j
//@Component
public class Spreading {
/*
    private final EdgeInfo edgeInfo;

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    // 다른 엣지로부터 데이터를 전달받은 KETI Server가 호출할 소켓
    // 이후
    public void afterGettingDataFromEdge() {
        try {
            int socketPort = 17300;
            ServerSocket serverSocket = new ServerSocket(socketPort);
            Socket socketClient = null; // client 접속시 사용
            log.info("socket :: " + socketPort + "open");

            while (true) {
                socketClient = serverSocket.accept();
                log.info("socket :: connected to client {}",socketClient.getPort());

                // cllient to server
                InputStream input = socketClient.getInputStream(); // socket의 inputstream 정보
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                String clientMsg = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

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
            pw.println("{[{REQ::10.2.0.7::001:: EDGE_LIST}]}");
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
                if (socket != null) {
                    socket.close();
                }
                if (br != null) {
                    br.close();
                }
                if (pw != null) {
                    pw.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }

 */
}
