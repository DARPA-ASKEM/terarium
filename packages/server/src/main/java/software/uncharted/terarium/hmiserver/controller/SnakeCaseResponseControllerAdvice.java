package software.uncharted.terarium.hmiserver.controller;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import jakarta.annotation.PostConstruct;
import software.uncharted.terarium.hmiserver.annotations.AMRPropertyNamingStrategy;

@RestControllerAdvice
public class SnakeCaseResponseControllerAdvice implements ResponseBodyAdvice {
	private ObjectMapper mapper;

	@PostConstruct
	public void init() {
		mapper = new ObjectMapper()
				.setPropertyNamingStrategy(
						new AMRPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()));
	}

	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		return returnType.getParameterType().isAssignableFrom(ResponseEntity.class);
	}

	private boolean containsKeyIgnoreCase(HttpHeaders headers, String key) {
		return headers.keySet().stream()
				.anyMatch(k -> k.equalsIgnoreCase(key));
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

		if (body != null && selectedContentType == MediaType.APPLICATION_JSON
				&& containsKeyIgnoreCase(request.getHeaders(), "X-Enable-Snake-Case")) {
			try {
				return mapper.readValue(mapper.writeValueAsString(body), JsonNode.class);
			} catch (JsonProcessingException ignored) {
			}
		}
		return body;
	}

}
