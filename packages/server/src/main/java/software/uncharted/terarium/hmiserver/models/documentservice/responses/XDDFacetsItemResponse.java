package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XDDFacetsItemResponse implements Serializable {

  @JsonProperty("doc_count_error_upper_bound")
  private Number docCountErrorUpperBound;

  @JsonProperty("sum_other_doc_count")
  private Number sumOtherDocCount;

  // The 'Object' in question here is a Number, however, sometimes they are coming in as Big
  // numbers, and the
  // parent class can't handle the deserialization of scientific notation.
  private List<XDDFacetBucket> buckets;
}
