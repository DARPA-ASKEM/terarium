package software.uncharted.terarium.hmiserver.service.data;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class NetCDFServiceTest extends TerariumApplicationTests {
	@Autowired
	private NetCDFService netCDFService;

	//@Test
	public void testFileStream() {
		File file = new File("/Users/ccoleman/Downloads/prra_Omon_IPSL-CM6A-LR_historical_r22i1p1f1_gr_185001-201412.nc");
		if (file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				Assertions.
			}

		} else {
		 Assertions.assertTrue(false);
		}
	}
}
