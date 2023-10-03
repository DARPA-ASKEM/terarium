package software.uncharted.terarium.hmiserver.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.uncharted.terarium.hmiserver.interceptors.OrderedHandlerInterceptor;

import java.util.Comparator;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TerariumWebConfiguration implements WebMvcConfigurer {
	private final List<OrderedHandlerInterceptor> interceptorList;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		interceptorList.stream()
			.sorted(Comparator.comparingInt(OrderedHandlerInterceptor::getOrder))
			.forEach(registry::addInterceptor);
	}
}
