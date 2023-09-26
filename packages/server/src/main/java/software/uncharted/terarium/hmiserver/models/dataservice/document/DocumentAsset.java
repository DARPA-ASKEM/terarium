package software.uncharted.terarium.hmiserver.models.dataservice.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;

import java.time.Instant;
import java.util.List;

@Data
@TSModel
@Accessors(chain = true)
public class DocumentAsset {

    @TSOptional
    private String id;

    @TSOptional
    private String name;

    @TSOptional
    private String description;

    @TSOptional
    private Instant timestamp;

    @TSOptional
    private String username;

    @TSOptional
    @JsonAlias("file_names")
    private List<String> fileNames;

    @TSOptional
    @JsonAlias("document_url")
    private List<String> documentUrl;

    @TSOptional
    private Object metadata;

    @TSOptional
    private String source;

    @TSOptional
    private String text;

    @TSOptional
    private Grounding grounding;

    @TSOptional
    private List<Concept> concepts;

}
