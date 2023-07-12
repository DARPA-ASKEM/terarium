package software.uncharted.terarium.hmiserver.models.documentservice;

import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDFacetsItemResponse;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SimilarDocuments implements Serializable {
    @JsonAlias("bibjsons")
    private List<Document> documents;

    @JsonAlias("facets")
    private Map<String, XDDFacetsItemResponse> facets;
}
