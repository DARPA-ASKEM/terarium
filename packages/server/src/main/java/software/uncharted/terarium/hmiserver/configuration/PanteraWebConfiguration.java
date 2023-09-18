package software.uncharted.terarium.hmiserver.configuration;

import com.google.common.reflect.ClassPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.uncharted.terarium.hmiserver.interceptors.OrderedHandlerInterceptor;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PanteraWebConfiguration implements WebMvcConfigurer {
  private final List<OrderedHandlerInterceptor> interceptorList;



  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    interceptorList.stream()
      .sorted(Comparator.comparingInt(OrderedHandlerInterceptor::getOrder))
      .forEach(registry::addInterceptor);
  }
	
}
