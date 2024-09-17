package software.uncharted.terarium.hmiserver.utils.rebac;

import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.SchemaServiceOuterClass.ReadSchemaRequest;
import com.authzed.api.v1.SchemaServiceOuterClass.ReadSchemaResponse;
import com.authzed.api.v1.SchemaServiceOuterClass.WriteSchemaRequest;
import com.authzed.api.v1.SchemaServiceOuterClass.WriteSchemaResponse;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaManager {

	public boolean doesSchemaExist(ManagedChannel channel, BearerToken bearerToken) throws Exception {
		SchemaServiceGrpc.SchemaServiceBlockingStub schemaService = SchemaServiceGrpc.newBlockingStub(
			channel
		).withCallCredentials(bearerToken);

		ReadSchemaRequest request = ReadSchemaRequest.newBuilder().build();

		log.info("Reading the Schema...");
		try {
			ReadSchemaResponse response = schemaService.readSchema(request);
			log.info("Reading Schema errors: {}", response.getInitializationErrorString());
			log.info("Schema:\n{}", response.getSchemaText());
			return true;
		} catch (StatusRuntimeException e) {
			if (e.getMessage().startsWith("NOT_FOUND:")) {
				return false;
			}
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	public void createSchema(ManagedChannel channel, BearerToken bearerToken, String schema) {
		SchemaServiceGrpc.SchemaServiceBlockingStub schemaService = SchemaServiceGrpc.newBlockingStub(
			channel
		).withCallCredentials(bearerToken);

		WriteSchemaRequest request = WriteSchemaRequest.newBuilder().setSchema(schema).build();

		WriteSchemaResponse response;
		response = schemaService.writeSchema(request);
		log.info("Create Schema: " + response.isInitialized());
	}
}
