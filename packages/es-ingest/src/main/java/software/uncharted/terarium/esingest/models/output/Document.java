package software.uncharted.terarium.esingest.models.output;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class Document implements Serializable {

	@Data
	static public class Paragraph implements Serializable {

		private String paragraphId;
		private double[] vector;
		Pair<Long, Long> spans;
	}

	private UUID id;

	private String title;

	private String fullText;

	private List<Paragraph> paragraphs;

}
