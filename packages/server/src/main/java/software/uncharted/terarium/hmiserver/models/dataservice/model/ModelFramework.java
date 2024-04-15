package software.uncharted.terarium.hmiserver.models.dataservice.model;

import jakarta.persistence.Entity;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAssetThatSupportsAdditionalProperties;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Accessors(chain = true)
@TSModel
public class ModelFramework extends TerariumAssetThatSupportsAdditionalProperties {

    @Serial
    private static final long serialVersionUID = -2375250471839320328L;

    private String name;

    private String version;

    private String semantics;
}
