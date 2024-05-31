package software.uncharted.terarium.hmiserver.models.dataservice;

import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileExport {

	@JsonDeserialize(using = ContentTypeDeserializer.class)
	ContentType contentType;

	byte[] bytes;
}
