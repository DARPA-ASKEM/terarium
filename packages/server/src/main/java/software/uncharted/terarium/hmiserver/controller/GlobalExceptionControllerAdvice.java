package software.uncharted.terarium.hmiserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.messaging.handler.invocation.MethodArgumentResolutionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionControllerAdvice {

	// Log common 4XX errors
	@ExceptionHandler(
		{
			MethodArgumentNotValidException.class,
			HttpMessageConversionException.class,
			HttpRequestMethodNotSupportedException.class,
			ServletRequestBindingException.class,
			MethodArgumentResolutionException.class,
			NoHandlerFoundException.class
		}
	)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handleBadRequest(final Exception ex) {
		log.error("HTTP Status Code: 400", ex);
	}
}
