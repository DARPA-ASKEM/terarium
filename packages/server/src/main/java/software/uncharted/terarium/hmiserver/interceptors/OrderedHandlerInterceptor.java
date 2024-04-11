package software.uncharted.terarium.hmiserver.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public interface OrderedHandlerInterceptor extends HandlerInterceptor {
  default int getOrder() {
    return 0;
  }
}
