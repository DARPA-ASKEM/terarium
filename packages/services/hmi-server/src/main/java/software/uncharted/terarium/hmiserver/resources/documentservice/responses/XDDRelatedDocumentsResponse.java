package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.documentservice.RelatedDocument;


import java.util.List;

@Data
@Accessors(chain = true)
public class XDDRelatedDocumentsResponse {

	private String status;

	private List<RelatedDocument> data;


}
