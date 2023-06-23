package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;

@Data
@Accessors(chain = true)
public class Observable {
    private String id;
    private String name;
    private List<String> states;
    private String expression;
    @JsonAlias("expression_mathml")
    private String expressionMathml;
}
