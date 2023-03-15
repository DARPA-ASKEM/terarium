package software.uncharted.terarium.hmiserver.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import javax.ws.rs.core.Response;

public class HmiResponseExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

	@Override
	public RuntimeException toThrowable(Response response) {
		//TODO we can/should intelligently handle these error codes and create nice thrown exceptions for them.
		if (response.getStatus() >= 400 && response.getStatus() < 500) {
			throw new ResponseRuntimeException("The remote service responded with HTTP " + response.getStatus(), response.getStatus());
		} else if (response.getStatus() >= 500) {
			throw new ResponseRuntimeException("The remote service responded with HTTP " + response.getStatus(), response.getStatus());
		}
		return null;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public class ResponseRuntimeException extends RuntimeException {
		private int status;

		public ResponseRuntimeException(String message, int status) {
			super(message);
			this.status = status;
		}
	}
}
