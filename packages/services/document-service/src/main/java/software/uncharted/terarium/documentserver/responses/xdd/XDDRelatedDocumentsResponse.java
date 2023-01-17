package software.uncharted.terarium.documentserver.responses.xdd;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.documentserver.models.xdd.RelatedDocument;

import java.util.List;

@Data
@Accessors(chain = true)
public class XDDRelatedDocumentsResponse {

	private String status;

	private List<RelatedDocument> data;

	public List<RelatedDocument> getData(){ return this.data; }

}
