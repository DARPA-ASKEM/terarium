package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class Concept {
	@JsonbProperty("term_id")
	public String termId;

	@JsonbProperty("status")
	public OntologicalField status;

	public Concept(final String termId, final OntologicalField status) {
		this.termId = termId;
		this.status = status;
	}
}
