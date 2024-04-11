package software.uncharted.terarium.esingest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"local", "test"})
public class ElasticIngestApplicationTests {

  @Test
  void contextLoads() {}
}
