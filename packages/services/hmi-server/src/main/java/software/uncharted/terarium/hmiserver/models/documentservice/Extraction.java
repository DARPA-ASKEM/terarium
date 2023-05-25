package software.uncharted.terarium.hmiserver.models.documentservice;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * XDD Document extraction representation
 */
@Data
@Accessors(chain = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)

public class Extraction implements Serializable {

	private Long id;

	@JsonAlias("ASKEM_CLASS")
	private String askemClass;


	private ExtractionProperties properties;

	@JsonAlias({"askem_id", "ASKEM_ID"})
	private String askemId;

	@JsonAlias("_xdd_created")
	private Date xddCreated;

	@JsonAlias("_xdd_registrant")
	private Number xddRegistrant;

	@JsonAlias("_highlight")
	private List<String> highlight;

}

