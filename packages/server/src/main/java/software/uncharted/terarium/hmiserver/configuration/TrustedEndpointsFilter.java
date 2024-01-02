package software.uncharted.terarium.hmiserver.configuration;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.security.web.util.OnCommittedResponseWrapper;

import java.io.IOException;

@Slf4j
public class TrustedEndpointsFilter implements Filter {

	static private int trustedPortNum;
	static private String trustedPathPrefix;

	TrustedEndpointsFilter(String trustedPort, String trustedPathPrefix) {
		if (trustedPort != null && trustedPathPrefix != null && !"null".equals(trustedPathPrefix)) {
			trustedPortNum = Integer.valueOf(trustedPort);
			this.trustedPathPrefix = trustedPathPrefix;
		}
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		if (trustedPortNum != 0) {

			if (isRequestForTrustedEndpoint(servletRequest) && servletRequest.getLocalPort() != trustedPortNum) {
				log.warn("denying request for trusted endpoint on untrusted port");
				((OnCommittedResponseWrapper) servletResponse).setStatus(403);
				servletResponse.getOutputStream().close();
				return;
			}

			if (!isRequestForTrustedEndpoint(servletRequest) && servletRequest.getLocalPort() == trustedPortNum) {
				log.warn("denying request for untrusted endpoint on trusted port");
				((OnCommittedResponseWrapper) servletResponse).setStatus(403);
				servletResponse.getOutputStream().close();
				return;
			}
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

	private boolean isRequestForTrustedEndpoint(ServletRequest servletRequest) {
		return ((SecurityContextHolderAwareRequestWrapper) servletRequest).getRequestURI().startsWith(trustedPathPrefix);
	}
}
