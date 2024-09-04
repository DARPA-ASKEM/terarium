package software.uncharted.terarium.hmiserver.models;

import java.sql.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;

@Slf4j
public class TerariumAssetTests extends TerariumApplicationTests {

	@Test
	void testSerializeWithoutTerariumFields() throws Exception {
		final Model model1 = new Model();
		model1.setName("myname");
		model1.setDescription("mydescription");
		model1.setFileNames(null);
		model1.setDeletedOn(null);
		model1.setTemporary(false);
		model1.setPublicAsset(false);
		model1.setUserId("my user");
		model1.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		model1.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		model1.setHeader(
			new ModelHeader()
				.setName("test-name")
				.setModelSchema("test-schema")
				.setModelVersion("0.1.2")
				.setDescription("test-description")
				.setSchemaName("petrinet")
		);

		final String json1 = model1.serializeWithoutTerariumFields();

		final Model model2 = new Model();
		model2.setName("myname");
		model2.setDescription("mydescription 2");
		model2.setFileNames(null);
		model2.setDeletedOn(null);
		model2.setTemporary(false);
		model2.setPublicAsset(false);
		model2.setUserId("my user 2");
		model2.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		model2.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		model2.setHeader(
			new ModelHeader()
				.setName("test-name")
				.setModelSchema("test-schema")
				.setModelVersion("0.1.2")
				.setDescription("test-description")
				.setSchemaName("petrinet")
		);

		final String json2 = model2.serializeWithoutTerariumFields();

		log.info("json1: {}", json1);

		Assertions.assertEquals(json1, json2);
	}
}
