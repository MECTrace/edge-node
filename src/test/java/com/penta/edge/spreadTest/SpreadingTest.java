package com.penta.edge.spreadTest;
import com.penta.edge.controller.Spreading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class SpreadingTest {

    @Autowired
    Spreading spreading;

    @Test
    public void start() {
        spreading.getDeviceInfo();
    }


}
