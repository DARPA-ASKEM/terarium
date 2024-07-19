package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import software.uncharted.terarium.hmiserver.annotations.AMRPropertyNamingStrategy;

@RestControllerAdvice
public class SnakeCaseResponseControllerAdvice implements ResponseBodyAdvice {

	@Autowired
	private ObjectMapper mapper;

	@PostConstruct
	public void init() {
		// We modify the injected object mappers because Spring injects an ObjectMapper
		// different from one that is returned via `new ObjectMapper()`
		mapper = mapper
			.copy()
			.setPropertyNamingStrategy(new AMRPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()));
	}

	@Override
	public boolean supports(final MethodParameter returnType, final Class converterType) {
		return returnType.getParameterType().isAssignableFrom(ResponseEntity.class);
	}

	private boolean containsKeyIgnoreCase(final HttpHeaders headers, final String key) {
		return headers.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(key));
	}

	@Override
	public Object beforeBodyWrite(
		final Object body,
		final MethodParameter returnType,
		final MediaType selectedContentType,
		final Class selectedConverterType,
		final ServerHttpRequest request,
		final ServerHttpResponse response
	) {
		if (
			body != null &&
			selectedContentType == MediaType.APPLICATION_JSON &&
			containsKeyIgnoreCase(request.getHeaders(), "X-Enable-Snake-Case")
		) {
			try {
				return mapper.readValue(mapper.writeValueAsString(body), JsonNode.class);
			} catch (final JsonProcessingException ignored) {}
		}
		return body;
	}
}
