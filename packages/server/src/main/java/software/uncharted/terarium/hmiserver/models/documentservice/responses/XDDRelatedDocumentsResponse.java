package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.documentservice.RelatedDocument;

@Data
@Accessors(chain = true)
public class XDDRelatedDocumentsResponse {

  private String status;

  private List<RelatedDocument> data;
}
