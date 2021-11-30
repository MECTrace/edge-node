package com.penta.edge.spreadTest;


import com.penta.edge.constant.EdgeNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class ETET {


    @Test
    public void test2() throws IOException {
        Socket socket = new Socket("localhost", 17300);
        OutputStream output = socket.getOutputStream();

        // byte[] data = {0x02, 0x10, 0x11, 0x01, 0x01, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xCF, (byte) 0xEC, 0x03};
        byte[] data = "{[{ANS::20.194.98.12::007::Successes::6ed763afcf250364df4d60ab8b526eff647f7fb4d344217b89536c6dc736f0e8}]}".getBytes(StandardCharsets.US_ASCII);
        // byte[] data = {0x7B, 0x50, 0x51, 0x52, 0x7D};

        output.write(data);

        socket.close();

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
