package software.uncharted.terarium.hmiserver.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;

@Slf4j
@Component
public class RequestLoggingInterceptor implements OrderedHandlerInterceptor {

	private static final String PRE_REQUEST_PREFIX = "REQUEST STARTED";
	private static final String POST_REQUEST_PREFIX = "REQUEST COMPLETED";
	private static final String PROPERTY_SEPARATOR = " | ";

	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

	@SuppressWarnings("unused")
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod) {
			if (skipLogging((HandlerMethod) handler)) {
				return true;
			}

			long startTime = System.nanoTime();
			request.setAttribute("startTime", startTime);

			String methodName = getMethodName((HandlerMethod) handler).toString();
			String requestMsg =
				PRE_REQUEST_PREFIX + PROPERTY_SEPARATOR + methodName + PROPERTY_SEPARATOR + getRequestInfo(request);

			MDC.put("method", methodName);
			logger.info(requestMsg);

			return true;
		} else if (handler instanceof ResourceHttpRequestHandler resourceHandler) {
			// Find the resource path of this handler and print it to the log
			// logger.info(resourceHandler.toString());
			return true;
		}

		return false;
	}

	@Override
	public void postHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler,
		ModelAndView modelAndView
	) {
		if (handler instanceof HandlerMethod) {
			if (skipLogging((HandlerMethod) handler)) {
				return;
			}
			long startTime = (long) request.getAttribute("startTime");
			long elapsedTime = System.nanoTime() - startTime;
			long millis = elapsedTime / 1000000;

			String requestMsg =
				POST_REQUEST_PREFIX +
				PROPERTY_SEPARATOR +
				getMethodName((HandlerMethod) handler) +
				PROPERTY_SEPARATOR +
				millis +
				" ms" +
				PROPERTY_SEPARATOR +
				getRequestInfo(request);

			MDC.put("response_time_ms", Long.toString(millis));
			logger.info(requestMsg);
		}
	}

	private boolean skipLogging(HandlerMethod handler) {
		return handler.getMethod().getAnnotation(IgnoreRequestLogging.class) != null;
	}

	private StringBuilder getMethodName(HandlerMethod handlerMethod) {
		StringBuilder methodNameBuilder = new StringBuilder();

		String controllerName = handlerMethod.getBeanType().getSimpleName();
		String methodName = handlerMethod.getMethod().getName();
		methodNameBuilder.append(controllerName).append('.').append(methodName);
		return methodNameBuilder;
	}

	private StringBuilder getRequestInfo(HttpServletRequest request) {
		final String REQUEST_INFO_SEPARATOR = "; ";
		StringBuilder requestInfoBuilder = new StringBuilder();

		if (request.getMethod() != null) {
			requestInfoBuilder.append("method=").append(request.getMethod()).append(REQUEST_INFO_SEPARATOR);
		}

		requestInfoBuilder.append("uri=").append(request.getRequestURI());
		if (request.getQueryString() != null) {
			String queryStr = request.getQueryString();
			requestInfoBuilder.append('?').append(queryStr);
		}
		requestInfoBuilder.append(REQUEST_INFO_SEPARATOR);

		return requestInfoBuilder;
	}
}
