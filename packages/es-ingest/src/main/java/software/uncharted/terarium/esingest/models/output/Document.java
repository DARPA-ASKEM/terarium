package software.uncharted.terarium.esingest.models.output;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class Document implements IOutputDocument, Serializable {

	@Data
	static public class Paragraph implements Serializable {

		private String paragraphId;
		private double[] vector;
		private long[] spans;
	}

	private UUID id;

	private String title;

	private String fullText;

	private List<Paragraph> paragraphs;

}
