package com.penta.edge.spreadTest;

import com.penta.edge.domain.Hash;
import com.penta.edge.process.EdgeProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class EdgeToCentralAuthTest {

    @Autowired
    EdgeProcess edgeProcess;

    @Test
    public void sendHash() {


        edgeProcess.sendHashToCentral(Hash.builder()
                .dataId("asdfasdfasedtsete")
                .destinationId("asdfasdf")
                .sourceId("zzzzz")
                .timestamp(LocalDateTime.now())
                .build());

    }
}
