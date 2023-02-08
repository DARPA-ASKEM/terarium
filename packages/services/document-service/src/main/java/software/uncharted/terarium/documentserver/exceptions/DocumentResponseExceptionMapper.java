package software.uncharted.terarium.documentserver.exceptions;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import javax.ws.rs.core.Response;

public class DocumentResponseExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

	@Override
	public RuntimeException toThrowable(Response response) {
		//TODO we can/should intelligently handle these error codes and create nice thrown exceptions for them.
		if (response.getStatus() >= 400 && response.getStatus() < 500) {
			throw new RuntimeException("The remote service responded with HTTP " + response.getStatus());
		} else if (response.getStatus() >= 500) {
			throw new RuntimeException("The remote service responded with HTTP " + response.getStatus());
		}
		return null;
	}
}
