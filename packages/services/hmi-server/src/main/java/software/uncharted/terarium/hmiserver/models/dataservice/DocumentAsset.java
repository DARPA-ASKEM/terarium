package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
public class DocumentAsset {
	private String id;

	private String name;

    private String username;

	@TSOptional
	private String description;
    
	private String timestamp;

	private String[] file_names;

    private Object metadata;

    private String document_url;

    private String source;

    private String text;

    private Object grounding;

    private Object[] assets;

}
