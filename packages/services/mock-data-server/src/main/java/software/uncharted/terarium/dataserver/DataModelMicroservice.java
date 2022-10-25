package software.uncharted.terarium.mockdataserver;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
@OpenAPIDefinition(
	info = @Info(
		title = "Data Model Microservice",
		description = "This is the data model microservice for the TERArium application",
		version = "1.0.0"
	),
	tags = {
		@Tag(name = "data", description = "Data model API endpoints")
	}
)
public class DataModelMicroservice extends Application {
}
