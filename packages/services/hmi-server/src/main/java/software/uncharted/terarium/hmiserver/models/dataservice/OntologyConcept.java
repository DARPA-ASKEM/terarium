package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;

public class OntologyConcept {

	@JsonbProperty("id")
	public String id = null;

	@JsonbProperty("term_id")
	public String termId;

	@JsonbProperty("type")
	public TaggableType type;

	@JsonbProperty("obj_id")
	public Long objId;

	@JsonbProperty("status")
	public OntologicalField status;

	public OntologyConcept(final String termId, final TaggableType type, final Long objId, final OntologicalField status) {
		this.termId = termId;
		this.type = type;
		this.objId = objId;
		this.status = status;
	}
}
