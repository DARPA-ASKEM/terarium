package software.uncharted.pantera.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "example", url = "https://example.com")
public interface ExampleProxy {
  @GetMapping
  String getExample();
}
