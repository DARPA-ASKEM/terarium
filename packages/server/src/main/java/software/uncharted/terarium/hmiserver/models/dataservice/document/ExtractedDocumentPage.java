package software.uncharted.terarium.hmiserver.models.dataservice.document;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExtractedDocumentPage implements Serializable {

	@Serial
	private static final long serialVersionUID = 7781295818378531195L;

	Integer pageNumber;

	String text;
	List<JsonNode> tables = new ArrayList<>();

	List<JsonNode> equations = new ArrayList<>();

	@Override
	public ExtractedDocumentPage clone() {
		final ExtractedDocumentPage clone = new ExtractedDocumentPage();

		clone.pageNumber = this.pageNumber;
		clone.text = this.text;

		clone.tables = new ArrayList<>();
		for (final JsonNode table : this.tables) {
			clone.tables.add(table.deepCopy());
		}

		clone.equations = new ArrayList<>();
		for (final JsonNode equation : this.equations) {
			clone.equations.add(equation.deepCopy());
		}

		return clone;
	}
}
