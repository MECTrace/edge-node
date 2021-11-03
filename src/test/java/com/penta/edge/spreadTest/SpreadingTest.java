package com.penta.edge.spreadTest;

import com.penta.edge.constant.EdgeInfo;
import com.penta.edge.constant.EdgeNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
public class SpreadingTest {


    @Autowired
    EdgeInfo edgeInfo;

    @Test
    public void test() {
        Random random = new Random();
        EdgeNode edgeNode;
        while (true) {
            EdgeNode edge = EdgeNode.values()[random.nextInt(4)];
            System.out.println("랜덤 :: "+edge.getIP());
            if (!edge.getIP().equals(edgeInfo.getIP())) {
                System.out.println("나 :: " + edgeInfo.getIP());
                edgeNode = edge;
                break;
            }
        }
    }
}
