package org.socius.sociuswebbackend;

import org.junit.jupiter.api.Test;
import org.socius.sociuswebbackend.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class SociusWebBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}