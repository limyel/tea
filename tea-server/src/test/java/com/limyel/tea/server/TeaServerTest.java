package com.limyel.tea.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeaServerTest {

    @Test
    public void testStart() {
        TeaServer.start(5940);
    }
}