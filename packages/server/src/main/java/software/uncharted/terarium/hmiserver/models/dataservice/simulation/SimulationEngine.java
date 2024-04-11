package software.uncharted.terarium.hmiserver.models.dataservice.simulation;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum SimulationEngine {
  @JsonAlias("sciml")
  SCIML,
  @JsonAlias("ciemss")
  CIEMSS
}
