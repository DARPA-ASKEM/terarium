package software.uncharted.terarium.hmiserver.utils.rebac;

import javax.enterprise.context.ApplicationScoped;

import com.authzed.api.v1.PermissionService.Consistency;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@ApplicationScoped
public class ReBACService {
    private BearerToken bearerToken = new BearerToken("somerandomkeyhere");
		private SchemaManager schemaManager = new SchemaManager();

		private boolean schemaExists = false;

		private void createSchemaIfNotExists() throws Exception {
				if( !schemaManager.doesSchemaExist(channel, bearerToken) ) {
						schemaManager.createSchema(channel, bearerToken, Schema.schema);
				}
				schemaExists = true;
		}

    private ManagedChannel channel = ManagedChannelBuilder
        .forTarget("localhost:50051")
        //.useTransportSecurity() // for TLS communication
        .usePlaintext()
        .build();

		public boolean canRead(String datumId, AskemDatumType datumType, String userId) throws Exception {
			if (!schemaExists) { createSchemaIfNotExists(); }

			SchemaObject user = new SchemaObject(Schema.Type.USER, userId);
			SchemaObject datum = new SchemaObject(Schema.Type.DATUM, datumType + datumId);
			Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();

			ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
			return rebac.checkPermission(user, Schema.Permission.READ, datum, full);
		}

		public boolean canWrite(String datumId, AskemDatumType datumType, String userId) throws Exception {
				if (!schemaExists) { createSchemaIfNotExists(); }

        SchemaObject user = new SchemaObject(Schema.Type.USER, userId);
        SchemaObject datum = new SchemaObject(Schema.Type.DATUM, datumType + datumId);
        Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();

        ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
        return rebac.checkPermission(user, Schema.Permission.WRITE, datum, full);
    }

		public boolean canAdministrate(String datumId, AskemDatumType datumType, String userId) throws Exception {
			if (!schemaExists) { createSchemaIfNotExists(); }

			SchemaObject user = new SchemaObject(Schema.Type.USER, userId);
			SchemaObject datum = new SchemaObject(Schema.Type.DATUM, datumType + datumId);
			Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();

			ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
			return rebac.checkPermission(user, Schema.Permission.ADMINISTRATE, datum, full);
		}

		public void createRelationship(String userId, Schema.Relationship relationship, String datumId, AskemDatumType datumType) throws Exception {
			if (!schemaExists) { createSchemaIfNotExists(); }

			SchemaObject user = new SchemaObject(Schema.Type.USER, userId);
			SchemaObject datum = new SchemaObject(Schema.Type.DATUM, datumType + datumId);

			ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
			rebac.createRelationship(user, relationship, datum);
		}
}
