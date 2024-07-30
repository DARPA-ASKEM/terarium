package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.http.entity.ContentType;

@Data
@Accessors(chain = true)
public class FileExport {

	@JsonDeserialize(using = ContentTypeDeserializer.class)
	ContentType contentType;

	String pathPrefix;

	@JsonIgnore
	byte[] bytes;
}
