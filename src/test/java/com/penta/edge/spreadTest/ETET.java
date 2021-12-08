package com.penta.edge.spreadTest;


import com.penta.edge.constant.EdgeNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;


public class ETET {

    /*
    @Autowired
    FileManager fileManager;

    @Test
    public void checkFileHash() {
        File f = new File("/Users/penta/device_target_file/vnv/5.1_Hyundai-Kona-Kia-Niro-BMS_33라8981_KNACC81GFKA024613_2021-08-13T08_00_00.000.csv.gz");
        String hash = fileManager.getHash(f);
        System.out.println(hash);

    }

     */


    @Test
    public void test3()  {
        OutputStream output = null;
        InputStream input = null;
        String hostname = "52.141.2.70";
        int port = 16300;
        int timeout = 3000;
        SocketAddress socketAddress = new InetSocketAddress(hostname, port);

        try (Socket socket = new Socket();) {


            socket.setSoTimeout(timeout);    //inputstream 에서 데이터를 읽을때의 timeout
            socket.connect(socketAddress, timeout);
            output = socket.getOutputStream();

            //{[{REQ::ID::REQ_CODE::REQ_DATA}]}
            // String reqString = "{[{REQ::" + edge.getIP() + "::007::" + dataid + "}]}";
            String reqString = "{[{REQ::52.141.20.211::007::028fe93c3da0ac7d3a8c47351983a2aa37f374f162b5a23aaf0cfcd041b05992}]}";
            byte[] data = reqString.getBytes(StandardCharsets.US_ASCII);
            output.write(data);
            output.flush();


            input = socket.getInputStream();

            byte[] bytes = new byte[200];
            int readByteCount = input.read(bytes);
            String receivedMsg = new String(bytes, 0, readByteCount, "UTF-8");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Test
    public void test2() throws IOException {
        /*
        Socket socket = new Socket("20.194.98.12", 16300);
        OutputStream output = socket.getOutputStream();

        // byte[] data = {0x02, 0x10, 0x11, 0x01, 0x01, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xCF, (byte) 0xEC, 0x03};
        byte[] data = "{[{REQ::20.194.98.12::007::6ed763afcf250364df4d60ab8b526eff647f7fb4d344217b89536c6dc736f0e8}]}".getBytes(StandardCharsets.US_ASCII);
        // byte[] data = {0x7B, 0x50, 0x51, 0x52, 0x7D};
        output.write(data);
        InputStream input = socket.getInputStream();
        byte[] bytes = new byte[200];
        int readByteCount = input.read(bytes);
        String receivedMsg = new String(bytes, 0, readByteCount, "UTF-8");
        System.out.println(receivedMsg);
        socket.close();

         */

    }




    @Test
    public void test() {
        int last = 0;
        while (last <=20) {

            Random random = new Random();
            EdgeNode[] edgeNode = new EdgeNode[2];
            int idx = 0;
            while (idx <= 1) {
                EdgeNode edge = EdgeNode.values()[random.nextInt(4)];
                if (!edge.getIP().equals("20.194.98.12")) {
                    if (idx == 0 || (idx == 1 && !edgeNode[0].getIP().equals(edge.getIP()))) {
                        edgeNode[idx] = edge;
                        idx++;
                    }
                }
            }

            System.out.println();
            Arrays.stream(edgeNode).forEach(i -> System.out.print(i.getIP() + " / "));
            System.out.println();
            last ++;

        }

    }


}
