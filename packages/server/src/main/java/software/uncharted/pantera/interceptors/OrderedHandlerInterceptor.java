package software.uncharted.pantera.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public interface OrderedHandlerInterceptor extends HandlerInterceptor {
  default int getOrder() {
    return 0;
  }
}
