package software.uncharted.terarium.hmiserver.utils.rebac.askem;

import com.authzed.api.v1.Core;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

public class RebacPermissionRelationship {

	Schema.Type subjectType;
	String subjectId;
	Schema.Relationship relationship;
	Schema.Type resourceType;
	String resourceId;

	public RebacPermissionRelationship(Core.ObjectReference subject, String relationship, Core.ObjectReference resource) {
		this.subjectType = Schema.Type.valueOf(subject.getObjectType().toUpperCase());
		this.subjectId = subject.getObjectId();
		this.relationship = Schema.Relationship.valueOf(relationship.toUpperCase());
		this.resourceType = Schema.Type.valueOf(resource.getObjectType().toUpperCase());
		this.resourceId = resource.getObjectId();
	}

	public Schema.Type getSubjectType() {
		return subjectType;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public Schema.Relationship getRelationship() {
		return relationship;
	}
}
