package software.uncharted.terarium.hmiserver.models.documentservice;


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

	private String askemClass;


	private ExtractionProperties properties;

	private String askemId;

	private Date xddCreated;

	private Number xddRegistrant;


	@JsonbProperty("ASKEM_CLASS")
	public void setAskemClass(String askemClass) {
		this.askemClass = askemClass;
	}

	@JsonbProperty("askem_id")
	public void setAskemId(String askemId) {
		this.askemId = askemId;
	}

	@JsonbProperty("_xdd_created")
	public void setXddCreated(Date xddCreated) {
		this.xddCreated = xddCreated;
	}

	@JsonbProperty("_xdd_registrant")
	public void setXddRegistrant(Number xddRegistrant) {
		this.xddRegistrant = xddRegistrant;
	}

}

