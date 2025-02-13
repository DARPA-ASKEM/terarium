package software.uncharted.terarium.hmiserver.models.dataservice;

// import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TSModel
@Entity
public class ChartAnnotation extends TerariumAsset {

	// TODO: Add chart type enums once we have the full list of the charts we support

	// @TSModel
	// public enum ChartType {
	// 	@JsonAlias("variable")
	// 	VARIABLE,
	// 	@JsonAlias("parameter")
	// 	PARAMETER,
	// 	@JsonAlias("loss")
	// 	LOSS,
	// 	@JsonAlias("error")
	// 	ERROR
	// }

	private UUID nodeId;
	private UUID outputId;

	private UUID chartId;

	// TODO: add chart type to specify which type of chart this annotation is applied to
	// @Enumerated(EnumType.STRING)
	// private ChartType chartType;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode layerSpec;

	private boolean llmGenerated;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode metadata;
}
