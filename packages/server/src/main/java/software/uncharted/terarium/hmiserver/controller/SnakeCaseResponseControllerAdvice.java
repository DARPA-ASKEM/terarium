package software.uncharted.terarium.hmiserver.controller;

import org.springframework.core.MethodParameter;
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

@RestControllerAdvice
public class SnakeCaseResponseControllerAdvice implements ResponseBodyAdvice {
	private ObjectMapper mapper;

	@PostConstruct
	public void init() {
		mapper = new ObjectMapper()
				.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
	}

	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		return returnType.getParameterType().isAssignableFrom(ResponseEntity.class);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

		if (body != null && selectedContentType == MediaType.APPLICATION_JSON
				&& !request.getHeaders().containsKey("X-Enable-Camel-Case")) {
			try {
				return mapper.readValue(mapper.writeValueAsString(body), JsonNode.class);
			} catch (JsonProcessingException ignored) {
			}
		}
		return body;
	}

}
