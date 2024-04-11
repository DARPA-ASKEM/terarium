package software.uncharted.terarium.hmiserver.models.dataservice.person;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Person implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  private UUID id;

  private String name;

  private String email;

  @JsonAlias("org")
  private String organization;

  private String website;

  @JsonAlias("is_registered")
  private Boolean isRegistered;
}
