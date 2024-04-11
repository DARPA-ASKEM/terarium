package software.uncharted.terarium.hmiserver.models.dataservice.equation;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum EquationType {
  mathml("mathml"),
  latex("latex");

  public final String type;

  private EquationType(final String type) {
    this.type = type.toLowerCase();
  }

  @Override
  public String toString() {
    return type;
  }

  @JsonCreator
  public static EquationType fromString(final String type) {
    return Arrays.stream(EquationType.values())
        .filter(t -> t.type.equalsIgnoreCase(type))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown equation type: " + type));
  }
}
