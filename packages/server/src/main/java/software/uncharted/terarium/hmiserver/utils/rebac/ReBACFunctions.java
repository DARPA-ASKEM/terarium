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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.utils.rebac.RelationsipAlreadyExistsException.RelationshipAlreadyExistsException;
import software.uncharted.terarium.hmiserver.utils.rebac.askem.RebacPermissionRelationship;

@Slf4j
public class ReBACFunctions {

	private static final String HAS_PERMISSION = "PERMISSIONSHIP_HAS_PERMISSION";
	private static final String ALREADY_EXISTS_CREATE_RELATIONSHIP = "ALREADY_EXISTS: could not CREATE relationship";

	final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;

	public ReBACFunctions(final ManagedChannel channel, final BearerToken bearerToken) {
		this.permissionsService = PermissionsServiceGrpc.newBlockingStub(channel).withCallCredentials(bearerToken);
	}

	private static ObjectReference createObject(final String type, final String id) {
		return ObjectReference.newBuilder().setObjectType(type).setObjectId(id).build();
	}

	private static SubjectReference createSubject(final String type, final String id) {
		return SubjectReference.newBuilder().setObject(createObject(type, id)).build();
	}

	public boolean checkPermission(
		final SchemaObject subject,
		final Schema.Permission permission,
		final SchemaObject resource,
		final Consistency consistency
	) throws Exception {
		return checkPermission(
			subject.type.toString(),
			subject.id,
			permission.toString(),
			resource.type.toString(),
			resource.id,
			consistency
		);
	}

	public boolean checkPermission(
		final String subjectType,
		final String subjectId,
		final String permission,
		final String resourceType,
		final String resourceId,
		final Consistency consistency
	) throws Exception {
		final PermissionService.CheckPermissionRequest request = PermissionService.CheckPermissionRequest.newBuilder()
			.setConsistency(consistency)
			.setResource(createObject(resourceType, resourceId))
			.setSubject(createSubject(subjectType, subjectId))
			.setPermission(permission)
			.build();

		final PermissionService.CheckPermissionResponse response = permissionsService.checkPermission(request);
		return response.getPermissionship().name().equalsIgnoreCase(HAS_PERMISSION);
	}

	public String createRelationship(
		final SchemaObject subject,
		final Schema.Relationship relationship,
		final SchemaObject target
	) throws Exception, RelationshipAlreadyExistsException {
		return createRelationship(
			subject.type.toString(),
			subject.id,
			relationship.toString(),
			target.type.toString(),
			target.id
		);
	}

	public String createRelationship(
		final String subjectType,
		final String subjectId,
		final String relationship,
		final String targetType,
		final String targetId
	) throws Exception, RelationshipAlreadyExistsException {
		final PermissionService.WriteRelationshipsRequest request = PermissionService.WriteRelationshipsRequest.newBuilder()
			.addUpdates(
				RelationshipUpdate.newBuilder()
					.setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
					.setRelationship(
						Relationship.newBuilder()
							.setResource(createObject(targetType, targetId))
							.setRelation(relationship)
							.setSubject(createSubject(subjectType, subjectId))
							.build()
					)
					.build()
			)
			.build();

		try {
			final PermissionService.WriteRelationshipsResponse response = permissionsService.writeRelationships(request);
			return response.getWrittenAt().getToken();
		} catch (final Exception e) {
			if (e.getMessage().startsWith(ALREADY_EXISTS_CREATE_RELATIONSHIP)) {
				throw new RelationshipAlreadyExistsException(e);
			}
			throw e;
		}
	}

	public String removeRelationship(
		final SchemaObject subject,
		final Schema.Relationship relationship,
		final SchemaObject target
	) throws Exception, RelationshipAlreadyExistsException {
		return removeRelationship(
			subject.type.toString(),
			subject.id,
			relationship.toString(),
			target.type.toString(),
			target.id
		);
	}

	public String removeRelationship(
		final String subjectType,
		final String subjectId,
		final String relationship,
		final String targetType,
		final String targetId
	) throws Exception, RelationshipAlreadyExistsException {
		final PermissionService.DeleteRelationshipsRequest request =
			PermissionService.DeleteRelationshipsRequest.newBuilder()
				.setRelationshipFilter(
					RelationshipFilter.newBuilder()
						.setResourceType(targetType)
						.setOptionalResourceId(targetId)
						.setOptionalRelation(relationship)
						.setOptionalSubjectFilter(
							PermissionService.SubjectFilter.newBuilder()
								.setSubjectType(subjectType)
								.setOptionalSubjectId(subjectId)
								.build()
						)
						.build()
				)
				.build();

		try {
			final PermissionService.DeleteRelationshipsResponse response = permissionsService.deleteRelationships(request);
			return response.getDeletedAt().getToken();
		} catch (final Exception e) {
			if (e.getMessage().startsWith(ALREADY_EXISTS_CREATE_RELATIONSHIP)) {
				throw new RelationshipAlreadyExistsException(e);
			}
			throw e;
		}
	}

	public List<RebacPermissionRelationship> getRelationship(final SchemaObject resource, final Consistency consistency)
		throws Exception {
		final PermissionService.ReadRelationshipsRequest request = PermissionService.ReadRelationshipsRequest.newBuilder()
			.setConsistency(consistency)
			.setRelationshipFilter(
				RelationshipFilter.newBuilder().setResourceType(resource.type.toString()).setOptionalResourceId(resource.id)
			)
			.build();
		return getRelationship(request);
	}

	public List<RebacPermissionRelationship> getRelationship(final PermissionService.ReadRelationshipsRequest request)
		throws Exception {
		final List<RebacPermissionRelationship> relationships = new ArrayList<>();

		final Iterator<ReadRelationshipsResponse> iter = permissionsService.readRelationships(request);

		while (iter.hasNext()) {
			final PermissionService.ReadRelationshipsResponse response = iter.next();
			final ObjectReference subject = response.getRelationship().getSubject().getObject();
			final ObjectReference resource = response.getRelationship().getResource();
			final RebacPermissionRelationship rebacRelationship = new RebacPermissionRelationship(
				subject,
				response.getRelationship().getRelation(),
				resource
			);
			relationships.add(rebacRelationship);
		}
		return relationships;
	}

	public boolean hasRelationship(
		final SchemaObject who,
		final Schema.Relationship relationship,
		final SchemaObject what,
		final Consistency consistency
	) throws Exception {
		final PermissionService.ReadRelationshipsRequest request = PermissionService.ReadRelationshipsRequest.newBuilder()
			.setConsistency(consistency)
			.setRelationshipFilter(
				RelationshipFilter.newBuilder()
					.setResourceType(what.type.toString())
					.setOptionalResourceId(what.id)
					.setOptionalRelation(relationship.toString())
			)
			.build();
		final List<RebacPermissionRelationship> relationships = getRelationship(request);
		for (final RebacPermissionRelationship permissionRelationship : relationships) {
			if (
				Schema.Type.USER.equals(permissionRelationship.getSubjectType()) &&
				who.id.equals(permissionRelationship.getSubjectId())
			) {
				return true;
			}
		}
		return false;
	}

	public List<UUID> lookupResources(
		final Schema.Type resourceType,
		final Schema.Permission permission,
		final SchemaObject who,
		final Consistency consistency
	) throws Exception {
		final List<UUID> results = new ArrayList<>();

		final PermissionService.LookupResourcesRequest request = PermissionService.LookupResourcesRequest.newBuilder()
			.setConsistency(consistency)
			.setResourceObjectType(resourceType.toString())
			.setSubject(createSubject(who.type.toString(), who.id))
			.setPermission(permission.toString())
			.build();

		final Iterator<LookupResourcesResponse> iter = permissionsService.lookupResources(request);
		while (iter.hasNext()) {
			final PermissionService.LookupResourcesResponse response = iter.next();

			if (
				response.getPermissionshipValue() ==
				PermissionService.LookupPermissionship.LOOKUP_PERMISSIONSHIP_HAS_PERMISSION.getNumber()
			) {
				try {
					final UUID uuid = UUID.fromString(response.getResourceObjectId());
					results.add(uuid);
				} catch (final IllegalArgumentException e) {
					log.warn("Unable to parse resource object id as UUID", e);
				}
			}
		}
		return results;
	}

	public List<UUID> lookupResources(final Schema.Type resourceType, final Consistency consistency) throws Exception {
		final Set<UUID> results = new HashSet<>();

		final PermissionService.RelationshipFilter filter = PermissionService.RelationshipFilter.newBuilder()
			.setResourceType("project")
			.build();

		final PermissionService.ReadRelationshipsRequest request = PermissionService.ReadRelationshipsRequest.newBuilder()
			.setConsistency(consistency)
			.setRelationshipFilter(filter)
			.build();

		final Iterator<ReadRelationshipsResponse> iter = permissionsService.readRelationships(request);
		while (iter.hasNext()) {
			final ReadRelationshipsResponse response = iter.next();

			final UUID uuid = UUID.fromString(response.getRelationship().getResource().getObjectId());
			results.add(uuid);
		}
		return new ArrayList<>(results);
	}
}
