package software.uncharted.terarium.hmiserver.models.dataservice;

import org.apache.http.entity.ContentType;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileExport {

	byte[] bytes;
	ContentType contentType;
}
