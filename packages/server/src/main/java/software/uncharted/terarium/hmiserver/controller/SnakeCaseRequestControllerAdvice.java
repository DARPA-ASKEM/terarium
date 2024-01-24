package software.uncharted.terarium.hmiserver.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import jakarta.annotation.PostConstruct;
import software.uncharted.terarium.hmiserver.annotations.AMRPropertyNamingStrategy;

@RestControllerAdvice
public class SnakeCaseRequestControllerAdvice implements RequestBodyAdvice {
	private ObjectMapper snakecaseMapper;

	@Autowired
	private ObjectMapper camelcaseMapper;

	@PostConstruct
	public void init() {
		snakecaseMapper = new ObjectMapper()
				.setPropertyNamingStrategy(
						new AMRPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()));
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
			MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	private boolean containsKeyIgnoreCase(HttpHeaders headers, String key) {
		return headers.keySet().stream()
				.anyMatch(k -> k.equalsIgnoreCase(key));
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

		if (containsKeyIgnoreCase(inputMessage.getHeaders(), "X-Enable-Snake-Case")) {
			JsonNode root = snakecaseMapper.readTree(inputMessage.getBody());
			String body = camelcaseMapper.writeValueAsString(root);
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			return new HttpInputMessage() {
				@Override
				public InputStream getBody() throws IOException {
					return new ByteArrayInputStream(bytes);
				}

				@Override
				public HttpHeaders getHeaders() {
					// Preserve the original headers
					return inputMessage.getHeaders();
				}
			};
		}
		return inputMessage;
	}
}
