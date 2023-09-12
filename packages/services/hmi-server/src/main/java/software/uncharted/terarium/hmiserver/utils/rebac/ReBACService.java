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
			/*
				if( !schemaManager.doesSchemaExist(channel, bearerToken) ) {
						schemaManager.createSchema(channel, bearerToken, Schema.schema);
				}
			 */
			schemaExists = true;
		}

    private ManagedChannel channel = ManagedChannelBuilder
        .forTarget("localhost:50051")
        //.useTransportSecurity() // for TLS communication
        .usePlaintext()
        .build();

		public boolean canRead(SchemaObject who, SchemaObject what) throws Exception {
			if (!schemaExists) { createSchemaIfNotExists(); }

			Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();
			ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
			return rebac.checkPermission(who, Schema.Permission.READ, what, full);
		}

		public boolean canWrite(SchemaObject who, SchemaObject what) throws Exception {
				if (!schemaExists) { createSchemaIfNotExists(); }

        Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();
        ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
        return rebac.checkPermission(who, Schema.Permission.WRITE, what, full);
    }

		public boolean canAdministrate(SchemaObject who, SchemaObject what) throws Exception {
			if (!schemaExists) { createSchemaIfNotExists(); }

			Consistency full = Consistency.newBuilder().setFullyConsistent(true).build();
			ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
			return rebac.checkPermission(who, Schema.Permission.ADMINISTRATE, what, full);
		}

		public void createRelationship(SchemaObject who, SchemaObject what, Schema.Relationship relationship) throws Exception {
			if (!schemaExists) { createSchemaIfNotExists(); }

			ReBACFunctions rebac = new ReBACFunctions(channel, bearerToken);
			rebac.createRelationship(who, relationship, what);
		}

		public void getDatumDetails(String datumId, AskemDatumType datumType) throws Exception {
			if (!schemaExists) { createSchemaIfNotExists(); }

			SchemaObject datum = new SchemaObject(Schema.Type.DATUM, datumType + datumId);
		}
}
