package software.uncharted.terarium.hmiserver.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.AMRPropertyNamingStrategy;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class SnakeCaseRequestControllerAdvice extends RequestBodyAdviceAdapter {
	private final ObjectMapper snakecaseMapper;
	private final ObjectMapper camelcaseMapper;

	@PostConstruct
	public void init() {
		camelcaseMapper.setPropertyNamingStrategy(
				new AMRPropertyNamingStrategy(new PropertyNamingStrategies.LowerCamelCaseStrategy()));
		snakecaseMapper.setPropertyNamingStrategy(
				new AMRPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()));
	}

	@Override
	public boolean supports(
			final MethodParameter methodParameter,
			final Type targetType,
			final Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	private boolean containsKeyIgnoreCase(final HttpHeaders headers, final String key) {
		return headers.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(key));
	}

	@Override
	public HttpInputMessage beforeBodyRead(
			final HttpInputMessage inputMessage,
			final MethodParameter parameter,
			final Type targetType,
			final Class<? extends HttpMessageConverter<?>> converterType)
			throws IOException {

		if (containsKeyIgnoreCase(inputMessage.getHeaders(), "X-Enable-Snake-Case")) {
			final JsonNode root = snakecaseMapper.readTree(inputMessage.getBody());
			final String body = camelcaseMapper.writeValueAsString(root);
			final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
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
