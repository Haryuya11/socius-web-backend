package org.socius.sociuswebbackend;

import org.junit.jupiter.api.Test;
import org.socius.sociuswebbackend.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class SociusWebBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}