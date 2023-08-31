package software.uncharted.pantera.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.uncharted.pantera.interceptors.OrderedHandlerInterceptor;

import java.util.Comparator;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PanteraWebConfiguration implements WebMvcConfigurer {
  private final List<OrderedHandlerInterceptor> interceptorList;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    interceptorList.stream()
      .sorted(Comparator.comparingInt(OrderedHandlerInterceptor::getOrder))
      .forEach(registry::addInterceptor);
  }
}
