package software.uncharted.terarium.hmiserver.configuration;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.uncharted.terarium.hmiserver.interceptors.OrderedHandlerInterceptor;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TerariumWebConfiguration implements WebMvcConfigurer {

	private final List<OrderedHandlerInterceptor> interceptorList;

	final Config config;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		interceptorList
			.stream()
			.sorted(Comparator.comparingInt(OrderedHandlerInterceptor::getOrder))
			.forEach(registry::addInterceptor);
	}
}
