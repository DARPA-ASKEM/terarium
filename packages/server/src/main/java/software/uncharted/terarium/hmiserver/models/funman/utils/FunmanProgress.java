package software.uncharted.terarium.hmiserver.models.funman.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TSModel
public class FunmanProgress {
    private Double coverage_of_representable_space;
    private Double coverage_of_search_space;
    private Double progress;
}