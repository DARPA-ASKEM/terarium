package software.uncharted.terarium.hmiserver.models.extraction;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@Data
public class BBox {

	public Float left;
	public Float top;
	public Float right;
	public Float bottom;
}
