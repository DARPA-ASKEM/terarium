package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class XDDFacetsItemResponse implements Serializable {

	@JsonAlias("doc_count_error_upper_bound")
	private Number docCountErrorUpperBound;

	@JsonAlias("sum_other_doc_count")
	private Number sumOtherDocCount;

	// The 'Object' in question here is a Number, however, sometimes they are coming in as Big numbers, and the
	// parent class can't handle the deserialization of scientific notation.
	private List<Map<String, Object>> buckets;

}
