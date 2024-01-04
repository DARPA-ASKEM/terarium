package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class XDDUrlExtraction implements Serializable {

	private String url;

	@JsonAlias("resource_title")
	private String resourceTitle;

	@JsonAlias("extracted_from")
	private List<String> extractedFrom;


}
