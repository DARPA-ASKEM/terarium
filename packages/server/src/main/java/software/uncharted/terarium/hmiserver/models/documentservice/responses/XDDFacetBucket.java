package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDFacetBucket implements Serializable {
	private String key;

	@JsonAlias("doc_count")
	private String docCount;

}
