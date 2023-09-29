package software.uncharted.terarium.hmiserver.models.simulationservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
@Accessors(chain = true)
@TSModel
// Used to specify any interventions provided by the AMR and given to the simulation-service.
public class Intervention {
    private String name;
    private Integer timestep;
    private Double value;

    @Override
    public String toString(){
        return " { name: " + this.name + " timestep: " + timestep.toString() + " value: " + value.toString() + " } ";
    }
}