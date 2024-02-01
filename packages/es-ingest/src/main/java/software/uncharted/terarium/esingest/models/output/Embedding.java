package software.uncharted.terarium.esingest.models.output;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class Embedding<T> implements OutputInterface, Serializable {

	private UUID id;
	private T embedding;

}
