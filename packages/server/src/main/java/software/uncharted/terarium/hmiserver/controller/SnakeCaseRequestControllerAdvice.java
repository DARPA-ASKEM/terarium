package software.uncharted.terarium.hmiserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import software.uncharted.terarium.hmiserver.annotations.AMRPropertyNamingStrategy;

@RestControllerAdvice
@Slf4j
public class SnakeCaseRequestControllerAdvice extends RequestBodyAdviceAdapter {
  private ObjectMapper snakecaseMapper;
  private ObjectMapper camelcaseMapper;

  @PostConstruct
  public void init() {
    camelcaseMapper =
        new ObjectMapper()
            .setPropertyNamingStrategy(
                new AMRPropertyNamingStrategy(
                    new PropertyNamingStrategies.LowerCamelCaseStrategy()));
    snakecaseMapper =
        new ObjectMapper()
            .setPropertyNamingStrategy(
                new AMRPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()));
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  private boolean containsKeyIgnoreCase(HttpHeaders headers, String key) {
    return headers.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(key));
  }

  @Override
  public HttpInputMessage beforeBodyRead(
      HttpInputMessage inputMessage,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType)
      throws IOException {

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
