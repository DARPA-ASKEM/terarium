package software.uncharted.terarium.documentserver.models.xdd;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.util.Date;


/**
 * XDD Document extraction representation
 */
@Data
@Accessors(chain = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Extraction implements Serializable {

	@JsonbProperty("ASKEM_CLASS")
	private String askemClass;

	@JsonbProperty("properties")
	private ExtractionProperties properties;

	@JsonbProperty("askem_id")
	private String askemId;

	@JsonbProperty("_xdd_created")
	private Date xddCreated;

	@JsonbProperty("_xdd_registrant")
	private Number xddRegistrant;
}

