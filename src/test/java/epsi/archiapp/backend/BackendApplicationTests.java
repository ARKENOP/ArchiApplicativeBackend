package epsi.archiapp.backend;

import epsi.archiapp.backend.testsupport.BasePostgresContainerIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests extends BasePostgresContainerIT {

    @Test
    void contextLoads() {
    }

}
