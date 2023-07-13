package software.uncharted.terarium.hmiserver.proxies;

import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.services.StructuredLog;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

@Interceptor
@LogRestClientTime
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
@Slf4j
public class RestClientTimingInterceptor {
	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	StructuredLog structuredLog;
	@AroundInvoke
	public Object measureInvocationTime(InvocationContext context) throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			return context.proceed();
		} finally {
			final long endTime = System.currentTimeMillis();
			final long executionTime = endTime - startTime;
			final String user = securityIdentity.getPrincipal() != null ? securityIdentity.getPrincipal().getName() : null;
			structuredLog.log(StructuredLog.Type.PROXY_REQUEST, user,
				"method", context.getMethod().getName(),
				"class", context.getMethod().getDeclaringClass().getName(),
				"path", getPath(context),
				"type", getType(context),
				"group", context.getMethod().getAnnotation(LogRestClientTime.class).group().equals("") ? parentPackage(context) : context.getMethod().getAnnotation(LogRestClientTime.class).group(),
				"duration", executionTime);
		}
	}

	private String getType(InvocationContext context) {
		if (context.getMethod().getAnnotation(javax.ws.rs.GET.class) != null) {
			return "GET";
		} else if (context.getMethod().getAnnotation(javax.ws.rs.POST.class) != null) {
			return "POST";
		} else if (context.getMethod().getAnnotation(javax.ws.rs.PUT.class) != null) {
			return "PUT";
		} else if (context.getMethod().getAnnotation(javax.ws.rs.DELETE.class) != null) {
			return "DELETE";
		} else if (context.getMethod().getAnnotation(javax.ws.rs.HEAD.class) != null) {
			return "HEAD";
		} else if (context.getMethod().getAnnotation(javax.ws.rs.OPTIONS.class) != null) {
			return "OPTIONS";
		} else if (context.getMethod().getAnnotation(javax.ws.rs.PATCH.class) != null) {
			return "PATCH";
		}
		return null;
	}

	private String getPath(InvocationContext context) {
		String parentPath = getPath(context.getMethod().getDeclaringClass());
		String methodPath = getPath(context.getMethod());
		if (!parentPath.equals("")) {
			return parentPath + methodPath;
		}
		return methodPath;
	}

	private String getPath(Class<?> clazz) {
		if (clazz.getAnnotation(javax.ws.rs.Path.class) != null) {
			return clazz.getAnnotation(javax.ws.rs.Path.class).value();
		}
		return "";
	}
	private String getPath(Method method) {
		if (method.getAnnotation(javax.ws.rs.Path.class) != null) {
			return method.getAnnotation(javax.ws.rs.Path.class).value();
		}
		return "";
	}

	private String parentPackage(InvocationContext context) {
		final String fullyQualifiedPackage = context.getMethod().getDeclaringClass().getPackage().getName();
		return fullyQualifiedPackage.split("\\.")[fullyQualifiedPackage.split("\\.").length - 1];
	}
}




