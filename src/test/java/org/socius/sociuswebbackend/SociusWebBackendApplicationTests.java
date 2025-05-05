package org.socius.sociuswebbackend;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.socius.sociuswebbackend.config.TestConfig;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class SociusWebBackendApplicationTests {



    @Test
    void contextLoads() {
    }
}