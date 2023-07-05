package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;

@Data
@Accessors(chain = true)
public class Observable {
    private String id;

    @TSOptional
    private String name;

    private List<String> states;

    @TSOptional
    private String expression;

    @TSOptional
    @JsonAlias("expression_mathml")
    private String expression_mathml;
}
