package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.http.entity.ContentType;

@Data
@Accessors(chain = true)
public class FileExport {

	byte[] bytes;
	ContentType contentType;
}
