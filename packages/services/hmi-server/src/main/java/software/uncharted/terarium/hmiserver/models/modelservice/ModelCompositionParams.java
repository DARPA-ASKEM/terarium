package software.uncharted.terarium.hmiserver.models.modelservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ModelCompositionParams implements Serializable {
    private PetriNet modelA;
    private PetriNet modelB;
    private List<Map<String, String>> statesToMerge;
}
