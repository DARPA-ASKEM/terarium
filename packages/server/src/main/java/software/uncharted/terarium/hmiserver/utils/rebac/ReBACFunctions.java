package software.uncharted.terarium.hmiserver.utils.rebac;
import com.authzed.api.v1.Core.ObjectReference;
import com.authzed.api.v1.Core.Relationship;
import com.authzed.api.v1.Core.RelationshipUpdate;
import com.authzed.api.v1.Core.SubjectReference;
import com.authzed.api.v1.PermissionService;
import com.authzed.api.v1.PermissionService.Consistency;
import com.authzed.api.v1.PermissionService.LookupResourcesResponse;
import com.authzed.api.v1.PermissionService.ReadRelationshipsResponse;
import com.authzed.api.v1.PermissionService.RelationshipFilter;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacPermissionRelationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReBACFunctions {
	final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;

	public ReBACFunctions(ManagedChannel channel, BearerToken bearerToken) {
		this.permissionsService = PermissionsServiceGrpc
			.newBlockingStub(channel)
			.withCallCredentials(bearerToken);
	}

	private ObjectReference createObject(String type, String id) {
		return ObjectReference.newBuilder()
			.setObjectType(type)
			.setObjectId(id)
			.build();
	}

	private SubjectReference createSubject(String type, String id) {
		return SubjectReference.newBuilder()
			.setObject(createObject(type, id))
			.build();
	}

	private static String HAS_PERMISSION = "PERMISSIONSHIP_HAS_PERMISSION";

	public boolean checkPermission(SchemaObject subject, Schema.Permission permission, SchemaObject resource, Consistency consistency) throws Exception {
		return checkPermission(subject.type.toString(), subject.id, permission.toString(), resource.type.toString(), resource.id, consistency);
	}

	public boolean checkPermission(String subjectType, String subjectId, String permission, String resourceType, String resourceId, Consistency consistency) throws Exception {
		PermissionService.CheckPermissionRequest request = PermissionService.CheckPermissionRequest.newBuilder()
			.setConsistency(consistency)
			.setResource(createObject(resourceType, resourceId))
			.setSubject(createSubject(subjectType, subjectId))
			.setPermission(permission)
			.build();

		PermissionService.CheckPermissionResponse response = permissionsService.checkPermission(request);
		return response.getPermissionship().name().equalsIgnoreCase(HAS_PERMISSION);
	}

	public String createRelationship(SchemaObject subject, Schema.Relationship relationship, SchemaObject target) throws Exception, RelationshipAlreadyExistsException {
		return createRelationship(subject.type.toString(), subject.id, relationship.toString(), target.type.toString(), target.id);
	}

	public String createRelationship(String subjectType, String subjectId, String relationship, String targetType, String targetId) throws Exception, RelationshipAlreadyExistsException {
		PermissionService.WriteRelationshipsRequest request = PermissionService.WriteRelationshipsRequest.newBuilder()
			.addUpdates(
				RelationshipUpdate.newBuilder()
					.setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
					.setRelationship(
						Relationship.newBuilder()
							.setResource(createObject(targetType, targetId))
							.setRelation(relationship)
							.setSubject(createSubject(subjectType, subjectId))
							.build())
					.build())
			.build();

		try {
			PermissionService.WriteRelationshipsResponse response = permissionsService.writeRelationships(request);
			return response.getWrittenAt().getToken();
		} catch (Exception e) {
			if (e.getMessage().startsWith(ALREADY_EXISTS_CREATE_RELATIONSHIP)) {
				throw new RelationshipAlreadyExistsException(e);
			}
			throw e;
		}
	}

	public String removeRelationship(SchemaObject subject, Schema.Relationship relationship, SchemaObject target) throws Exception, RelationshipAlreadyExistsException {
		return removeRelationship(subject.type.toString(), subject.id, relationship.toString(), target.type.toString(), target.id);
	}

	private static final String ALREADY_EXISTS_CREATE_RELATIONSHIP = "ALREADY_EXISTS: could not CREATE relationship";

	public String removeRelationship(String subjectType, String subjectId, String relationship, String targetType, String targetId) throws Exception, RelationshipAlreadyExistsException {
		PermissionService.DeleteRelationshipsRequest request = PermissionService.DeleteRelationshipsRequest.newBuilder()
			.setRelationshipFilter(RelationshipFilter.newBuilder()
				.setResourceType(targetType)
				.setOptionalResourceId(targetId)
				.setOptionalRelation(relationship)
				.setOptionalSubjectFilter(PermissionService.SubjectFilter
					.newBuilder()
					.setSubjectType(subjectType)
					.setOptionalSubjectId(subjectId)
					.build()
				).build()
			).build();

		try {
			PermissionService.DeleteRelationshipsResponse response = permissionsService.deleteRelationships(request);
			return response.getDeletedAt().getToken();
		} catch (Exception e) {
			if (e.getMessage().startsWith(ALREADY_EXISTS_CREATE_RELATIONSHIP)) {
				throw new RelationshipAlreadyExistsException(e);
			}
			throw e;
		}
	}

	public List<RebacPermissionRelationship> getRelationship(SchemaObject resource, Consistency consistency) throws Exception {
		PermissionService.ReadRelationshipsRequest request = PermissionService.ReadRelationshipsRequest.newBuilder()
			.setConsistency(consistency)
			.setRelationshipFilter(
				RelationshipFilter.newBuilder()
					.setResourceType(resource.type.toString())
					.setOptionalResourceId(resource.id))
			.build();
		return getRelationship(request);
	}

	public List<RebacPermissionRelationship> getRelationship(PermissionService.ReadRelationshipsRequest request) throws Exception {
		List<RebacPermissionRelationship> relationships = new ArrayList<>();

		Iterator<ReadRelationshipsResponse> iter = permissionsService.readRelationships(request);

		while (iter.hasNext()) {
			PermissionService.ReadRelationshipsResponse response = iter.next();
			ObjectReference subject = response.getRelationship().getSubject().getObject();
			ObjectReference resource = response.getRelationship().getResource();
			RebacPermissionRelationship rebacRelationship = new RebacPermissionRelationship(subject, response.getRelationship().getRelation(), resource);
			relationships.add(rebacRelationship);
		}
		return relationships;
	}

	public boolean hasRelationship(SchemaObject who, Schema.Relationship relationship, SchemaObject what, Consistency consistency) throws Exception {
		PermissionService.ReadRelationshipsRequest request = PermissionService.ReadRelationshipsRequest.newBuilder()
			.setConsistency(consistency)
			.setRelationshipFilter(
				RelationshipFilter.newBuilder()
					.setResourceType(what.type.toString())
					.setOptionalResourceId(what.id)
					.setOptionalRelation(relationship.toString()))
			.build();
		List<RebacPermissionRelationship> relationships = getRelationship(request);
		for (RebacPermissionRelationship permissionRelationship: relationships) {
			if (Schema.Type.USER.equals(permissionRelationship.getSubjectType()) && who.id.equals(permissionRelationship.getSubjectId())) {
				return true;
			}
		}
		return false;
	}

	public List<String> lookupResources(Schema.Type resourceType, Schema.Permission permission, SchemaObject who, Consistency consistency) throws Exception {
		List<String> results = new ArrayList<>();

		PermissionService.LookupResourcesRequest request = PermissionService.LookupResourcesRequest.newBuilder()
			.setConsistency(consistency)
			.setResourceObjectType(resourceType.toString())
			.setSubject(createSubject(who.type.toString(), who.id))
			.setPermission(permission.toString())
			.build();

		Iterator<LookupResourcesResponse> iter = permissionsService.lookupResources(request);
		while (iter.hasNext()) {
			PermissionService.LookupResourcesResponse response = iter.next();

			if( response.getPermissionshipValue() == PermissionService.LookupPermissionship.LOOKUP_PERMISSIONSHIP_HAS_PERMISSION.getNumber()) {
				results.add(response.getResourceObjectId());
			}
		}
		return results;
	}
}
