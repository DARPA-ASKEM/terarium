package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Publication implements Serializable {

	private String id;

	private String xddUri;
	@JsonbProperty("xdd_uri")
	public void setXddUri(String xddUri){
			this.xddUri = xddUri;
	}
	public String getXddUri(){ return this.xddUri; }

	@JsonbProperty("title")
	private String title;

}
