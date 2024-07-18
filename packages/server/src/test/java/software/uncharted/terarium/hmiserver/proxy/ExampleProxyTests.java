package software.uncharted.terarium.hmiserver.proxy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;

@ExtendWith(OutputCaptureExtension.class)
public class ExampleProxyTests extends TerariumApplicationTests {

	@Autowired
	private ExampleProxy proxy;

	@Test
	public void testItCanGetExample() {
		final String example = proxy.getExample();
		Assertions.assertTrue(example.contains("This domain is for use in illustrative examples in documents"));
	}

	@Test
	public void testItLogsProxyRequests(final CapturedOutput output) {
		proxy.getExample();
		Assertions.assertTrue(output.getOut().contains("[ExampleProxy#getExample] --->"));
	}

	@Test
	public void testItLogsProxyResponses(final CapturedOutput output) {
		proxy.getExample();
		Assertions.assertTrue(output.getOut().contains("[ExampleProxy#getExample] <---"));
	}
}
