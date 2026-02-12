package com.nexashop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = NexaShopApplication.class)
class DigimartBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
