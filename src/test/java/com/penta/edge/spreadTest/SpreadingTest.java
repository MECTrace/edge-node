package com.penta.edge.spreadTest;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class SpreadingTest {

    @Test
    public void test() {
        LocalDateTime lc = LocalDateTime.now();
        System.out.println(lc.toString());
    }
}
